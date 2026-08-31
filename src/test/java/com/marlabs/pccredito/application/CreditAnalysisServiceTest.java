package com.marlabs.pccredito.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.marlabs.pccredito.api.request.CreditAnalysisRequest;
import com.marlabs.pccredito.application.port.SerasaCreditGateway;
import com.marlabs.pccredito.domain.CustomerRelationshipType;
import com.marlabs.pccredito.idempotency.IdempotencyService;
import com.marlabs.pccredito.idempotency.IdempotencyService.Reservation;
import com.marlabs.pccredito.integration.serasa.SerasaTokenManager;

class CreditAnalysisServiceTest {

	@Test
	void shouldOrchestrateUsingMockedGatewayOnly() {

		SerasaCreditGateway gateway =
				mock(SerasaCreditGateway.class);

		IdempotencyService idempotencyService =
				mock(IdempotencyService.class);

		SerasaTokenManager tokenManager =
				mock(SerasaTokenManager.class);

		when(
				idempotencyService.reserve(
						anyString(),
						anyString()
				)
		).thenAnswer(
				invocation ->
						new Reservation(
								invocation.getArgument(0),
								invocation.getArgument(1)
						)
		);

		when(tokenManager.getValidAccessToken())
				.thenReturn("fake-token");

		when(
				gateway.submit(
						any(),
						anyString(),
						anyString()
				)
		).thenReturn("fake-serasa-response");

		CreditAnalysisService service =
				new CreditAnalysisService(
						gateway,
						idempotencyService,
						tokenManager
				);

		CreditAnalysisRequest request =
				new CreditAnalysisRequest(
						"12345678000199",
						CustomerRelationshipType.NOVO,
						10000L,
						new BigDecimal("87.50"),
						12,
						new BigDecimal("15000.75"),
						new BigDecimal("2500.30")
				);

		String response =
				service.analyze(
						request,
						"idempotency-123"
				);

		assertEquals(
				"fake-serasa-response",
				response
		);

		verify(idempotencyService)
				.reserve(
						eq("idempotency-123"),
						anyString()
				);

		verify(tokenManager)
				.getValidAccessToken();

		verify(gateway)
				.submit(
						any(),
						eq("idempotency-123"),
						eq("fake-token")
				);

		verify(idempotencyService)
				.complete(any(Reservation.class));
	}
}