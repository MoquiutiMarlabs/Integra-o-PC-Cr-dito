package com.marlabs.pccredito.integration.serasa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.marlabs.pccredito.domain.CreditAnalysis;

class SerasaRequestMapperTest {

	private final SerasaRequestMapper mapper = new SerasaRequestMapper();

	@Test
	void shouldNotInventPendingBusinessCreditData() {
		CreditAnalysis analysis = new CreditAnalysis(
				"request-123", "12345678000199", new BigDecimal("10000.00"));

		PendingSerasaContractException exception = assertThrows(
				PendingSerasaContractException.class,
				() -> mapper.toNovaProposta(analysis, null));

		assertEquals("SERASA-CONTRACT-001", PendingSerasaContractException.CONTRACT_ID);
		assertEquals(true, exception.getMessage().contains(PendingSerasaContractException.CONTRACT_ID));
	}
}
