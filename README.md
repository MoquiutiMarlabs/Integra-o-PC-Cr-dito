# PC Crédito Integration

Camada de integração e orquestração responsável pela comunicação entre os sistemas
da Veste e a plataforma Serasa/PowerCurve.

```text
Veste / Linx
     |
     | JWT
     v
API Gateway Veste
     |
     v
PC Crédito Integration
     |
     | autenticação Serasa
     v
Serasa / PowerCurve
```

## Objetivo

A aplicação atua exclusivamente como uma camada de integração/orquestração.

Suas principais responsabilidades são:

- receber solicitações de análise de crédito da Veste;
- validar tecnicamente o contrato de entrada;
- controlar correlação das requisições;
- controlar idempotência;
- obter e reutilizar token de acesso da Serasa;
- transformar o contrato Veste para o contrato Serasa;
- consumir o serviço `NovaProposta` do PowerCurve;
- receber a resposta da Serasa;
- futuramente transformar o retorno Serasa para um contrato estável da Veste;
- padronizar tratamento técnico de erros;
- fornecer observabilidade da integração.

## O que esta aplicação NÃO faz

Esta aplicação **não é um motor de crédito**.

Portanto, não é responsabilidade desta aplicação:

- calcular score;
- aprovar ou reprovar crédito;
- determinar limite;
- definir política de crédito;
- reativar crédito;
- interpretar regras de decisão;
- reproduzir regras existentes no PowerCurve.

Decisões de crédito pertencem ao PowerCurve/Serasa ou ao sistema de negócio
formalmente definido pela Veste.

---

# Stack

Stack atualmente adotada:

- Java 25;
- Spring Boot 4.1.x;
- Maven;
- Spring Web;
- Spring Security;
- Bean Validation;
- Spring Actuator;
- Micrometer;
- JUnit 5;
- Mockito;
- WireMock.

Novas tecnologias devem ser adicionadas somente quando houver necessidade técnica
e compatibilidade validada com a stack do projeto.

---

# Arquitetura

Estrutura principal:

```text
src/main/java/com/marlabs/pccredito
|
+-- api
|   +-- controller
|   +-- exception
|   +-- request
|   +-- response
|
+-- application
|   +-- port
|
+-- config
|
+-- domain
|
+-- idempotency
|
+-- integration
|   +-- serasa
|       +-- dto
|
+-- observability
|
+-- security
|
+-- PcCreditoApplication.java
```

Responsabilidades:

### `api`

Contrato HTTP exposto para a Veste.

Contém controllers, requests, responses e tratamento padronizado de erros.

### `application`

Orquestra o caso de uso.

Coordena:

```text
request
  -> idempotência
  -> token Serasa
  -> gateway Serasa
  -> resposta
```

Não contém regras de decisão de crédito.

### `domain`

Representação interna dos dados necessários ao fluxo.

Não deve conhecer detalhes HTTP ou estruturas específicas como
`DV-Application` e `DV-Applicant`.

### `integration.serasa`

Fronteira de integração com Serasa/PowerCurve.

Responsável por:

- autenticação Serasa;
- gerenciamento de token;
- montagem do payload `NovaProposta`;
- chamada HTTP;
- recebimento da resposta;
- adaptação entre contratos.

### `security`

Controles de segurança da API.

A autenticação corporativa definitiva será realizada no ambiente da Veste através
do API Gateway e Microsoft Entra ID/SSO.

### `observability`

Correlação e rastreabilidade técnica das requisições.

### `idempotency`

Controle técnico para impedir processamento duplicado de operações capazes de gerar
uma nova proposta.

---

# Fluxo da operação

Fluxo esperado:

```text
1. Linx/Veste solicita token ao SSO da Veste
                       |
                       v
2. SSO emite JWT
                       |
                       v
3. Linx/Veste chama API Gateway com JWT
                       |
                       v
4. Gateway autentica e autoriza a chamada
                       |
                       v
5. POST /v1/credit-analyses
                       |
                       v
6. Correlation ID
                       |
                       v
7. Bean Validation
                       |
                       v
8. Idempotency-Key
                       |
                       v
9. Obtenção/reutilização do token Serasa
                       |
                       v
10. Mapper Veste -> Serasa
                       |
                       v
11. POST NovaProposta
                       |
                       v
12. PowerCurve processa a proposta
                       |
                       v
13. Resposta Serasa
                       |
                       v
14. Resposta para Veste
```

---

# Endpoint principal

```http
POST /v1/credit-analyses
Content-Type: application/json
Idempotency-Key: credito-12345678000199-001
X-Correlation-Id: <opcional>
```

Exemplo completo:

```json
{
  "cnpj": "12345678000199",
  "subproduto2": "Novo",
  "valorSolicitado": 10000,
  "pontualidadeInterna": 87.5,
  "mediaDiasAtraso": 12,
  "valorAVencer": 15000.75,
  "valorVencido": 2500.30
}
```

Também é permitido utilizar:

```json
{
  "cnpj": "12345678000199",
  "subproduto2": "Carteira",
  "valorSolicitado": 10000
}
```

Quando os quatro indicadores financeiros não forem informados, são utilizados os
valores default `0`, conforme confirmação formal da Serasa.

---

# Contrato de entrada

## `cnpj`

CNPJ do cliente PJ.

Formato atual:

```text
14 dígitos numéricos
```

Exemplo:

```json
"cnpj": "12345678000199"
```

## `subproduto2`

Identifica a relação do cliente com a Veste.

Valores aceitos:

```text
Novo
Carteira
```

Semântica confirmada:

- `Novo`: cliente novo para a Veste;
- `Carteira`: cliente que já pertence à carteira da Veste.

A definição de qual valor enviar pertence ao sistema Veste.

## `valorSolicitado`

Valor do empréstimo solicitado.

A Serasa confirmou que `ValorEmprestimoSolicitado` deve ser enviado como
**número inteiro, sem casas decimais**.

Exemplo:

```json
"valorSolicitado": 10000
```

## `pontualidadeInterna`

Valor percentual decimal entre `0` e `100`.

Exemplo:

```json
"pontualidadeInterna": 87.5
```

Quando não houver informação:

```text
0
```

## `mediaDiasAtraso`

Quantidade inteira de dias de atraso.

Exemplo:

```json
"mediaDiasAtraso": 12
```

Quando não houver informação:

```text
0
```

## `valorAVencer`

Valor decimal a vencer.

Exemplo:

```json
"valorAVencer": 15000.75
```

Quando não houver informação:

```text
0
```

## `valorVencido`

Valor decimal vencido.

Exemplo:

```json
"valorVencido": 2500.30
```

Quando não houver informação:

```text
0
```

A Serasa confirmou que campos vazios ou sem informação podem impactar o
processamento da estratégia. Por isso, quando esses quatro indicadores não estiverem
disponíveis, deve ser enviado o valor default `0`.

---

# Payload Serasa / NovaProposta

A aplicação transforma o contrato recebido da Veste para o contrato esperado pelo
PowerCurve.

Estrutura atual:

```json
{
  "DV-Application": {
    "IDservico": "NovaProposta",
    "Fonte": "12345678",
    "ProdutoSolicitado": {
      "Produto": "EMPR",
      "Subproduto1": "Industria",
      "Subproduto2": "Novo",
      "ValorEmprestimoSolicitado": 10000
    },
    "DadosEntradaPersonalizados": [
      {
        "Chave": "Pontualidade Interna",
        "Valor": "87.5"
      },
      {
        "Chave": "Media dias de atraso",
        "Valor": "12"
      },
      {
        "Chave": "Valor a vencer",
        "Valor": "15000.75"
      },
      {
        "Chave": "Valor vencido",
        "Valor": "2500.30"
      }
    ]
  },
  "DV-Applicant": {
    "Applicant": [
      {
        "CNPJ": "12345678000199"
      }
    ]
  }
}
```

Campos atualmente fixos no contrato de integração:

```text
IDservico   = NovaProposta
Fonte       = 12345678
Produto     = EMPR
Subproduto1 = Industria
```

`Subproduto2` é recebido da Veste e pode assumir `Novo` ou `Carteira`.

---

# Integração Serasa

A integração com a Serasa utiliza duas etapas independentes.

## 1. Autenticação

A aplicação solicita um access token utilizando as credenciais técnicas fornecidas
pela Serasa.

Essas credenciais devem existir exclusivamente em secret manager ou variáveis
seguras do ambiente.

Nunca devem ser:

- hardcoded;
- adicionadas ao Git;
- registradas em logs;
- retornadas pela API;
- compartilhadas entre ambientes.

## 2. NovaProposta

Após obter um token válido:

```text
PC Crédito
   |
   | Bearer <access-token>
   v
Serasa / PowerCurve / NovaProposta
```

O token é reutilizado enquanto for considerado válido.

Em caso de `401` causado por expiração ou invalidação, o comportamento planejado é:

```text
invalidar token local
        ->
obter novo token
        ->
repetir chamada no máximo uma vez
```

Nunca deve existir retry infinito de autenticação.

---

# Autenticação Veste -> API

A autenticação corporativa foi definida pela Veste.

A integração passará pelo **API Gateway da Veste**, com autenticação e autorização
através de JWT emitido pelo SSO corporativo/Microsoft Entra ID.

Fluxo informado:

```text
Sistema consumidor
       |
       | credenciais definidas pela Veste
       v
SSO Veste
       |
       | JWT
       v
API Gateway
       |
       | valida autenticação/autorização
       v
PC Crédito Integration
```

A configuração definitiva de:

- Authorization Server;
- issuer;
- audience;
- scopes;
- claims;
- políticas de autorização;

será definida pela Veste durante a implantação no ambiente corporativo.

A aplicação **não deve criar um mecanismo próprio de login** e nunca deve reutilizar
credenciais da Veste para autenticação na Serasa.

As duas fronteiras permanecem independentes:

```text
Veste -> PC Crédito
```

e

```text
PC Crédito -> Serasa
```

---

# Correlation ID

Toda requisição deve possuir um Correlation ID.

Header:

```http
X-Correlation-Id
```

Quando fornecido pelo consumidor, o valor é preservado.

Quando ausente, a aplicação gera um UUID.

O identificador é disponibilizado no MDC para permitir rastreamento técnico do fluxo.

Logs podem registrar:

- início da operação;
- serviço chamado;
- resultado técnico;
- status HTTP;
- duração;
- tipo de erro;
- correlation ID.

Nunca devem registrar:

- Authorization;
- access token;
- senha;
- client secret;
- payload integral contendo dados sensíveis.

---

# Idempotência

Operações capazes de gerar uma `NovaProposta` devem possuir identificador único.

Header:

```http
Idempotency-Key
```

Exemplo:

```text
credito-12345678000199-001
```

Objetivo:

```text
mesma solicitação
       +
retry / timeout
       =
não gerar duas propostas
```

A implementação definitiva para ambiente distribuído ainda depende da definição de
um armazenamento durável e compartilhado.

Não deve ser utilizado controle exclusivamente em memória como garantia de
idempotência em produção.

---

# Resiliência

Retry deve ser aplicado somente para falhas tecnicamente transitórias.

Candidatos:

```text
timeout
HTTP 429
HTTP 500
HTTP 502
HTTP 503
HTTP 504
```

Não executar retry automático para:

```text
HTTP 400
payload inválido
erro funcional
regra de negócio
falha permanente de autenticação
```

Circuit Breaker deverá proteger as chamadas externas quando a implementação de
resiliência for concluída.

---

# Configuração

Nenhum secret real deve existir no repositório.

Variáveis atualmente previstas:

| Variável | Uso |
|---|---|
| `SERASA_BASE_URL` | URL base Serasa |
| `SERASA_TOKEN_URL` | endpoint de autenticação Serasa |
| `SERASA_USERNAME` | usuário técnico Serasa |
| `SERASA_PASSWORD` | senha técnica Serasa |
| `SERASA_CLIENT_ID` | client ID Serasa |
| `SERASA_CLIENT_SECRET` | client secret Serasa |
| `SERASA_CONNECT_TIMEOUT` | timeout de conexão |
| `SERASA_READ_TIMEOUT` | timeout de leitura |
| `M2M_AUTHENTICATION_REQUIRED` | controle técnico de autenticação |
| `IP_WHITELIST_ENABLED` | habilita whitelist adicional |
| `VESTE_ALLOWED_IPS` | IPs permitidos quando aplicável |

Ambientes:

```text
application-dev.yml
application-hml.yml
application-prd.yml
```

Credenciais devem ser distintas e isoladas entre DEV, HML e PRD.

---

# Resposta da API

Durante a fase atual de integração, a resposta da Serasa está sendo devolvida pela
API para permitir validação ponta a ponta do fluxo.

Exemplo simplificado:

```json
{
  "serviceContextId": "...",
  "data": {
    "DV-Application": {
      "NumeroProposta": "...",
      "CodigoRecomendacaoProposta": "...",
      "NomeRecomendacaoProposta": "..."
    }
  }
}
```

**Este passthrough não representa necessariamente o contrato definitivo da Veste.**

O contrato definitivo:

```text
PC Crédito Integration -> Linx/Veste
```

ainda será formalizado.

A intenção arquitetural é evitar que o Linx fique diretamente acoplado ao contrato
interno da Serasa/PowerCurve.

---

# Situação atual da homologação

A comunicação técnica ponta a ponta está operacional:

```text
POST /v1/credit-analyses
        ->
obtenção do token Serasa
        ->
montagem NovaProposta
        ->
chamada PowerCurve
        ->
recebimento da resposta
        ->
retorno ao consumidor
```

Foram validados tecnicamente os valores:

```text
Subproduto2 = Novo
Subproduto2 = Carteira
```

e o PowerCurve confirmou o recebimento dos respectivos valores na resposta.

No momento, o PowerCurve encerra o processamento PJ com:

```text
O produto pedido não está disponível,
por favor envie um produto válido no seu pedido
```

Portanto, permanece pendente confirmar com Serasa/Veste a combinação válida no
ambiente de homologação para:

```text
Produto
Subproduto1
Subproduto2
```

A aplicação não deve inventar ou alterar essa combinação sem confirmação formal.

---

# Pendências

## SERASA-PRODUCT-001

Confirmar a combinação válida para o fluxo PJ no ambiente de homologação:

```text
Produto = EMPR
Subproduto1 = Industria
Subproduto2 = Novo | Carteira
```

Status:

```text
PENDENTE
```

## VESTE-CONTRACT-001

Definir contrato definitivo de resposta:

```text
PC Crédito Integration -> Linx/Veste
```

Necessário definir:

- campos necessários ao Linx;
- tipos;
- obrigatoriedade;
- semântica;
- tratamento de ausência;
- estrutura de sucesso;
- estrutura de erro;
- versionamento.

Status:

```text
PENDENTE
```

## VESTE-AUTH-001

Autenticação corporativa definida em alto nível:

```text
SSO Veste -> JWT -> API Gateway -> PC Crédito
```

Issuer, audience, scopes, claims e demais parâmetros serão configurados pela Veste
durante a implantação.

Status:

```text
DEFINIDO EM ALTO NÍVEL / CONFIGURAÇÃO PENDENTE
```

## IDEMPOTENCY-001

Implementar armazenamento durável, compartilhado e atômico para idempotência antes
da disponibilização produtiva.

Status:

```text
PENDENTE
```

## RESILIENCE-001

Implementar e validar:

- timeout;
- retry seletivo;
- exponential backoff;
- circuit breaker;
- renovação controlada de token após 401.

Status:

```text
PENDENTE
```

---

# Executando localmente

Requer JDK 25.

Executar testes:

```powershell
.\mvnw.cmd clean test
```

Executar aplicação no perfil DEV:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Endpoint local:

```text
POST http://localhost:8080/v1/credit-analyses
```

---

# Testes

O projeto utiliza:

- JUnit 5;
- Mockito;
- WireMock.

Os testes devem cobrir principalmente:

- validação do contrato de entrada;
- mapeamento Veste -> Serasa;
- serialização do payload Serasa;
- autenticação Serasa;
- gerenciamento de token;
- orquestração do caso de uso;
- correlation ID;
- tratamento técnico de erros;
- idempotência.

Nenhum teste unitário deve depender da disponibilidade real da Serasa.

---

# Princípios do projeto

1. Não implementar decisão de crédito.
2. Não inventar campos ou regras Serasa.
3. Não preencher lacunas contratuais silenciosamente.
4. Manter Veste e Serasa como fronteiras de segurança independentes.
5. Nunca registrar secrets ou tokens.
6. Não expor detalhes desnecessários da Serasa ao consumidor.
7. Aplicar retry somente em falhas transitórias.
8. Evitar duplicação de propostas.
9. Toda requisição deve ser rastreável por Correlation ID.
10. Toda decisão contratual externa relevante deve possuir evidência formal.
11. DEV, HML e PRD devem possuir configurações e credenciais isoladas.
12. A camada de integração deve permanecer simples e não assumir responsabilidades
    de um motor de crédito.