package com.marlabs.pccredito.application;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.marlabs.pccredito.api.request.CreditAnalysisRequest;
import com.marlabs.pccredito.api.response.CreditAnalysisResponse;
import com.marlabs.pccredito.application.port.SerasaCreditGateway;
import com.marlabs.pccredito.domain.CreditAnalysis;
import com.marlabs.pccredito.idempotency.IdempotencyService;
import com.marlabs.pccredito.idempotency.IdempotencyService.Reservation;
import com.marlabs.pccredito.integration.serasa.SerasaTokenManager;

@Service
public class CreditAnalysisService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CreditAnalysisService.class);

	private final SerasaCreditGateway serasaCreditGateway;
	private final IdempotencyService idempotencyService;
	private final SerasaTokenManager serasaTokenManager;

	public CreditAnalysisService(
			SerasaCreditGateway serasaCreditGateway,
			IdempotencyService idempotencyService,
			SerasaTokenManager serasaTokenManager) {
		this.serasaCreditGateway = serasaCreditGateway;
		this.idempotencyService = idempotencyService;
		this.serasaTokenManager = serasaTokenManager;
	}

	public CreditAnalysisResponse analyze(
			CreditAnalysisRequest request,
			String idempotencyKey) {

		long startedAt = System.nanoTime();

		String requestId = UUID.randomUUID().toString();

		CreditAnalysis analysis = new CreditAnalysis(
				requestId,
				request.cnpj(),
				request.valorSolicitado()
		);

		Reservation reservation =
				idempotencyService.reserve(idempotencyKey, requestId);

		LOGGER.info(
				"credit_analysis_started requestId={} service=serasa",
				requestId
		);

		try {

			String accessToken =
					serasaTokenManager.getValidAccessToken();

			serasaCreditGateway.submit(
					analysis,
					idempotencyKey,
					accessToken
			);

			idempotencyService.complete(reservation);

			LOGGER.info(
					"credit_analysis_finished requestId={} service=serasa technicalResult=success durationMs={}",
					requestId,
					elapsedMillis(startedAt)
			);

			return new CreditAnalysisResponse(
					requestId,
					"ACCEPTED"
			);

		} catch (RuntimeException exception) {

			idempotencyService.fail(reservation);

			LOGGER.warn(
					"credit_analysis_finished requestId={} service=serasa technicalResult=error durationMs={} errorType={}",
					requestId,
					elapsedMillis(startedAt),
					exception.getClass().getSimpleName()
			);

			throw exception;
		}
	}

	private long elapsedMillis(long startedAt) {
		return (System.nanoTime() - startedAt) / 1_000_000;
	}
}

