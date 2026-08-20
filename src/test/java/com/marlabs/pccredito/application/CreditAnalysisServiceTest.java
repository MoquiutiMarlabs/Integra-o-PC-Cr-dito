package com.marlabs.pccredito.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.marlabs.pccredito.api.request.CreditAnalysisRequest;
import com.marlabs.pccredito.api.response.CreditAnalysisResponse;
import com.marlabs.pccredito.application.port.SerasaCreditGateway;
import com.marlabs.pccredito.idempotency.IdempotencyService;
import com.marlabs.pccredito.idempotency.IdempotencyService.Reservation;

class CreditAnalysisServiceTest {

	@Test
	void shouldOrchestrateUsingMockedGatewayOnly() {
		SerasaCreditGateway gateway = mock(SerasaCreditGateway.class);
		IdempotencyService idempotencyService = mock(IdempotencyService.class);
		when(idempotencyService.reserve(anyString(), anyString()))
				.thenAnswer(invocation -> new Reservation(invocation.getArgument(0), invocation.getArgument(1)));
		CreditAnalysisService service = new CreditAnalysisService(gateway, idempotencyService);
		CreditAnalysisRequest request = new CreditAnalysisRequest(
				"12345678000199", new BigDecimal("10000.00"));

		CreditAnalysisResponse response = service.analyze(request, "idempotency-123");

		assertEquals("ACCEPTED", response.status());
		verify(idempotencyService).reserve("idempotency-123", response.requestId());
		verify(gateway).submit(any(), org.mockito.ArgumentMatchers.eq("idempotency-123"));
		verify(idempotencyService).complete(any(Reservation.class));
	}
}
