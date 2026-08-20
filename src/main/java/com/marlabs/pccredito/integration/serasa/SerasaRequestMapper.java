package com.marlabs.pccredito.integration.serasa;

import org.springframework.stereotype.Component;

import com.marlabs.pccredito.domain.BusinessCreditData;
import com.marlabs.pccredito.domain.CreditAnalysis;
import com.marlabs.pccredito.integration.serasa.dto.SerasaNovaPropostaRequest;

@Component
public class SerasaRequestMapper {

	public SerasaNovaPropostaRequest toNovaProposta(
			CreditAnalysis analysis,
			BusinessCreditData businessCreditData) {
		// TODO SERASA-CONTRACT-001: map Pontualidade Interna, Media dias de atraso,
		// Valor a vencer and Valor vencido only after their ownership and requiredness
		// are formally confirmed. Never supply defaults from the example collection.
		throw new PendingSerasaContractException();
	}
}

