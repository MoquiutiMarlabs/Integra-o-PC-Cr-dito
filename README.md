# pc-credito-integration

Camada de integração e orquestração do fluxo:

```text
Veste -> API Marlabs -> Serasa/PowerCurve -> API Marlabs -> Veste
```

O projeto usa Java 25, Spring Boot 4.1.0 e Maven.

## Objetivo e responsabilidades

- receber solicitações técnicas de análise de crédito da Veste;
- validar o contrato HTTP de entrada;
- controlar correlação e preparar idempotência;
- traduzir modelos internos para contratos Serasa em uma fronteira isolada;
- orquestrar autenticação e chamadas Serasa quando os contratos estiverem formalizados;
- devolver respostas técnicas sem expor detalhes internos.

## O que a aplicação NÃO faz

Esta aplicação não é um motor de crédito. Ela não calcula score ou limite, não
aprova, reprova ou reativa crédito e não cria regras financeiras. Toda decisão de
crédito pertence ao PowerCurve/Serasa ou ao sistema de negócio formalmente definido.

Nenhum valor observado em collections ou exemplos é tratado como default ou regra.

## API inicial

```http
POST /v1/credit-analyses
Content-Type: application/json
Idempotency-Key: valor-opcional
X-Correlation-Id: valor-opcional
```

```json
{
  "cnpj": "12345678000199",
  "valorSolicitado": 10000.00
}
```

O CNPJ e o valor são obrigatórios, e o valor deve ser maior que zero. O endpoint
não contém decisão de crédito. Enquanto `SERASA-CONTRACT-001` estiver aberta, uma
tentativa de integração termina tecnicamente com HTTP 503 e nenhuma chamada externa.

## Arquitetura macro

- `api`: controllers, contratos públicos e tratamento de erros;
- `application`: orquestração dos casos de uso e portas de saída;
- `domain`: modelos internos neutros;
- `integration.serasa`: adaptação exclusiva do contrato e autenticação Serasa;
- `security`: segurança HTTP e whitelist adicional;
- `observability`: correlação de requisições;
- `idempotency`: ciclo de reserva técnica para prevenção de duplicidade;
- `config`: propriedades e cliente HTTP.

O domínio não conhece JSON Serasa, OAuth, HTTP, PowerCurve ou nomes como
`DV-Application`, `DV-Applicant` e `DadosEntradaPersonalizados`.

## Segurança

A configuração padrão, HML e PRD é *fail-closed*: apenas o health check é público e
os demais endpoints permanecem negados até a definição formal da autenticação
machine-to-machine. Exclusivamente no perfil `dev`, `/v1/credit-analyses` é liberado
sem autenticação para testes locais via Postman.

TODO: configurar OAuth2 Client Credentials/JWT para a fronteira Veste -> Marlabs
quando issuer, audience e claims forem confirmados. A credencial recebida da Veste
nunca será reutilizada na fronteira Marlabs -> Serasa.

A whitelist é opcional, vem de configuração externa e é somente um controle
adicional; ela nunca substitui autenticação. Não se confia em headers de proxy para
determinar o IP até a topologia de rede ser formalizada.

## Configuração e variáveis de ambiente

Nenhum secret real é armazenado no repositório. Cada ambiente deve fornecer suas
próprias credenciais por variáveis de ambiente ou secret manager:

| Variável | Uso |
|---|---|
| `SERASA_BASE_URL` | URL base da API Serasa |
| `SERASA_TOKEN_URL` | URL de obtenção de token |
| `SERASA_USERNAME` | usuário técnico |
| `SERASA_PASSWORD` | senha técnica |
| `SERASA_CLIENT_ID` | identificador OAuth |
| `SERASA_CLIENT_SECRET` | secret OAuth |
| `SERASA_CONNECT_TIMEOUT` | timeout de conexão, padrão técnico `3s` |
| `SERASA_READ_TIMEOUT` | timeout de resposta, padrão técnico `10s` |
| `M2M_AUTHENTICATION_REQUIRED` | exige autenticação M2M; padrão seguro `true` |
| `IP_WHITELIST_ENABLED` | habilita a whitelist adicional |
| `VESTE_ALLOWED_IPS` | IPs separados por vírgula |

Os arquivos `application-dev.yml`, `application-hml.yml` e `application-prd.yml`
identificam os perfis, mas não contêm credenciais. O provisionamento deve garantir
secrets distintos e isolados por ambiente.

## Observabilidade

`CorrelationIdFilter` preserva `X-Correlation-Id` quando recebido, gera UUID quando
ausente, disponibiliza o valor no MDC, devolve-o na resposta e permite propagação
no cliente HTTP.

Os logs registram início, serviço, resultado técnico, duração, tipo de erro e
correlation ID. Não devem registrar payload integral, `Authorization`, senha,
`client_secret` ou access token.

Actuator expõe `health`, `info` e `prometheus`; detalhes do health não são enviados
ao consumidor.

## Idempotência

`IdempotencyService` define reserva, conclusão e falha antes de operações capazes de
gerar `NovaProposta`. O skeleton não usa mapa em memória e não finge oferecer
idempotência distribuída.

TODO: implementar armazenamento durável, compartilhado e atômico antes de habilitar
`NovaProposta` ou retries em produção. A política de recuperação após timeout também
precisa ser formalizada para impedir propostas duplicadas.

## Resiliência

A fronteira Serasa documenta retry seletivo somente para timeout e HTTP 429, 500,
502, 503 e 504. Não haverá retry automático para HTTP 400, payload inválido, erro
funcional, regra de negócio ou autenticação inválida recorrente.

Em um 401 causado por expiração ou invalidação, o token poderá ser invalidado,
renovado e a chamada original repetida no máximo uma vez. Não haverá loop, retry
indiscriminado, persistência ou logging do token.

Resilience4j ainda não foi adicionado: a compatibilidade de um artefato com Spring
Boot 4.1 deve ser validada antes de escolher uma versão. Pelo mesmo motivo, OpenAPI,
WireMock e Testcontainers só serão adicionados quando houver versão compatível
validada e um teste que efetivamente necessite deles.

## Executar

Requer JDK 25:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Os testes atuais são locais, usam JUnit 5 e Mockito e não exigem banco, container,
rede ou chamada Serasa.

## Pendências externas

### SERASA-CONTRACT-001

Confirmar com a Serasa:

> A Veste enviará somente CNPJ + valor solicitado, ou também deverá fornecer
> Pontualidade Interna, Média dias de atraso, Valor a vencer e Valor vencido?

Esses atributos estão isolados em `BusinessCreditData` e não fazem parte do contrato
público `CreditAnalysisRequest`. A tradução para `DadosEntradaPersonalizados` fica
exclusivamente em `SerasaRequestMapper`.

Nenhuma decisão arquitetural de negócio será tomada por suposição enquanto a
resposta não chegar. Em particular, não serão usados os valores `100`, `0`, `0` e
`0` observados em exemplo, nem qualquer outro default inventado.

## Outros TODOs conhecidos

- confirmar o contrato exato de autenticação e de resposta OAuth da Serasa;
- confirmar campos, valores e obrigatoriedade do payload `NovaProposta`;
- implementar persistência de idempotência antes de habilitar chamadas/retries;
- formalizar autenticação M2M Veste -> Marlabs;
- validar compatibilidade de Resilience4j, OpenAPI, WireMock e Testcontainers com
  Spring Boot 4.1 antes de adicionar dependências.

