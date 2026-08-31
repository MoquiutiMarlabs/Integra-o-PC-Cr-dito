package com.marlabs.pccredito.integration.serasa;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import com.marlabs.pccredito.domain.CreditAnalysis;
import com.marlabs.pccredito.integration.serasa.dto.SerasaNovaPropostaRequest;

@Component
public class SerasaRequestMapper {

	private static final String SERVICE_ID = "NovaProposta";
	private static final String PRODUCT = "EMPR";
	private static final String SUBPRODUCT_1 = "Industria";

	private static final String INTERNAL_PUNCTUALITY =
			"Pontualidade Interna";

	private static final String AVERAGE_DELAY_DAYS =
			"Media dias de atraso";

	private static final String AMOUNT_DUE =
			"Valor a vencer";

	private static final String OVERDUE_AMOUNT =
			"Valor vencido";

	public SerasaNovaPropostaRequest toNovaProposta(
			CreditAnalysis analysis,
			String fonte) {

		var produto =
				new SerasaNovaPropostaRequest.ProdutoSolicitado(
						PRODUCT,
						SUBPRODUCT_1,
						analysis.customerRelationshipType().serasaValue(),
						analysis.requestedAmount()
				);

		List<SerasaNovaPropostaRequest.DadoEntradaPersonalizado>
				dadosEntradaPersonalizados = List.of(

				dado(
						INTERNAL_PUNCTUALITY,
						analysis.internalPunctuality()
				),

				dado(
						AVERAGE_DELAY_DAYS,
						analysis.averageDelayDays()
				),

				dado(
						AMOUNT_DUE,
						analysis.amountDue()
				),

				dado(
						OVERDUE_AMOUNT,
						analysis.overdueAmount()
				)
		);

		var application =
				new SerasaNovaPropostaRequest.Application(
						SERVICE_ID,
						fonte,
						produto,
						dadosEntradaPersonalizados
				);

		var applicant =
				new SerasaNovaPropostaRequest.Applicant(
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

	private SerasaNovaPropostaRequest.DadoEntradaPersonalizado dado(
			String chave,
			BigDecimal valor) {

		return new SerasaNovaPropostaRequest.DadoEntradaPersonalizado(
				chave,
				valor.stripTrailingZeros().toPlainString()
		);
	}

	private SerasaNovaPropostaRequest.DadoEntradaPersonalizado dado(
			String chave,
			Integer valor) {

		return new SerasaNovaPropostaRequest.DadoEntradaPersonalizado(
				chave,
				Integer.toString(valor)
		);
	}
}