package com.marlabs.pccredito.application.port;

import com.marlabs.pccredito.domain.CreditAnalysis;

public interface SerasaCreditGateway {

	String submit(
			CreditAnalysis analysis,
			String idempotencyKey,
			String accessToken
	);
}