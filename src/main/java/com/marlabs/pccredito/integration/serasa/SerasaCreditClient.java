package com.marlabs.pccredito.integration.serasa;

import org.springframework.stereotype.Component;

import com.marlabs.pccredito.application.port.SerasaCreditGateway;
import com.marlabs.pccredito.domain.CreditAnalysis;

@Component
public class SerasaCreditClient implements SerasaCreditGateway {

	private final SerasaRequestMapper requestMapper;

	public SerasaCreditClient(SerasaRequestMapper requestMapper) {
		this.requestMapper = requestMapper;
	}

	@Override
	public void submit(CreditAnalysis analysis, String idempotencyKey) {
		// Mapping intentionally fails with SERASA-CONTRACT-001 before any HTTP call.
		requestMapper.toNovaProposta(analysis, null);

		// TODO: enable NovaProposta only after the contract and durable idempotency are ready.
		// Selective retry may cover timeouts and HTTP 429/500/502/503/504 only.
		// HTTP 400, invalid payloads, functional failures and recurring authentication
		// errors must not be retried. A 401 token refresh may replay exactly once.
	}
}

