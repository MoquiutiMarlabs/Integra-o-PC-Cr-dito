package com.marlabs.pccredito.integration.serasa;

import com.marlabs.pccredito.domain.CreditAnalysis;
import com.marlabs.pccredito.integration.serasa.dto.SerasaNovaPropostaRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SerasaRequestMapper {

	public SerasaNovaPropostaRequest toNovaProposta(
			CreditAnalysis analysis,
			String fonte) {

		/*
		 * TODO SERASA-CONTRACT-001
		 *
		 * IMPORTANTE:
		 * Os valores abaixo estão sendo utilizados APENAS para reproduzir
		 * exatamente o payload PJ fornecido pela Serasa e validar tecnicamente
		 * a chamada NovaProposta.
		 *
		 * Eles NÃO devem ser tratados como defaults ou regra de negócio.
		 *
		 * Confirmar formalmente com a Serasa/Veste a origem de:
		 *
		 * - Pontualidade Interna
		 * - Media dias de atraso
		 * - Valor a vencer
		 * - Valor vencido
		 */

		long valorSolicitado;

		try {
			/*
			 * TODO SERASA-CONTRACT-002:
			 * Confirmar a unidade de ValorEmprestimoSolicitado.
			 *
			 * A API Serasa confirmou tecnicamente que o campo exige integer,
			 * porém ainda deve ser confirmado se o valor representa reais,
			 * centavos ou outra unidade contratual.
			 */
			valorSolicitado = analysis.requestedAmount().longValueExact();
		} catch (ArithmeticException exception) {
			throw new IllegalArgumentException(
					"Valor solicitado deve ser inteiro para o contrato atual da Serasa",
					exception
			);
		}

		var produto = new SerasaNovaPropostaRequest.ProdutoSolicitado(
				"EMPR",
				"Industria",
				"Novo",
				valorSolicitado
		);

		List<SerasaNovaPropostaRequest.DadoEntradaPersonalizado>
				dadosEntradaPersonalizados = List.of(

				new SerasaNovaPropostaRequest.DadoEntradaPersonalizado(
						"Pontualidade Interna",
						"100"
				),

				new SerasaNovaPropostaRequest.DadoEntradaPersonalizado(
						"Media dias de atraso",
						"0"
				),

				new SerasaNovaPropostaRequest.DadoEntradaPersonalizado(
						"Valor a vencer",
						"0"
				),

				new SerasaNovaPropostaRequest.DadoEntradaPersonalizado(
						"Valor vencido",
						"0"
				)
		);

		var application = new SerasaNovaPropostaRequest.Application(
				"NovaProposta",
				fonte,
				produto,
				dadosEntradaPersonalizados
		);

		var applicant = new SerasaNovaPropostaRequest.Applicant(
				analysis.cnpj()
		);

		var applicantContainer =
				new SerasaNovaPropostaRequest.ApplicantContainer(
						List.of(applicant)
				);

		return new SerasaNovaPropostaRequest(
				application,
				applicantContainer
		);
	}
}