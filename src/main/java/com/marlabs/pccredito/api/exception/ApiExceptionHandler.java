package com.marlabs.pccredito.api.exception;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import com.marlabs.pccredito.integration.serasa.InvalidSerasaResponseException;
import com.marlabs.pccredito.integration.serasa.PendingSerasaContractException;
import com.marlabs.pccredito.observability.CorrelationIdFilter;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ApiExceptionHandler {

	private static final Logger LOGGER =
			LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiError> handleValidation(
			MethodArgumentNotValidException exception,
			HttpServletRequest request) {

		Map<String, String> fieldErrors = new LinkedHashMap<>();

		exception.getBindingResult()
				.getFieldErrors()
				.forEach(error -> fieldErrors.putIfAbsent(
						error.getField(),
						error.getDefaultMessage()
				));

		return error(
				HttpStatus.BAD_REQUEST,
				"VALIDATION_ERROR",
				"Request validation failed",
				fieldErrors,
				request
		);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<ApiError> handleUnreadableMessage(
			HttpMessageNotReadableException exception,
			HttpServletRequest request) {

		return error(
				HttpStatus.BAD_REQUEST,
				"MALFORMED_REQUEST",
				"Request body is malformed",
				Map.of(),
				request
		);
	}

	@ExceptionHandler(PendingSerasaContractException.class)
	ResponseEntity<ApiError> handlePendingContract(
			PendingSerasaContractException exception,
			HttpServletRequest request) {

		LOGGER.warn(
				"request_failed technicalResult=pending_contract contractId={}",
				PendingSerasaContractException.CONTRACT_ID
		);

		return error(
				HttpStatus.SERVICE_UNAVAILABLE,
				PendingSerasaContractException.CONTRACT_ID,
				"External contract confirmation is pending",
				Map.of(),
				request
		);
	}

	@ExceptionHandler(InvalidSerasaResponseException.class)
	ResponseEntity<ApiError> handleInvalidSerasaResponse(
			InvalidSerasaResponseException exception,
			HttpServletRequest request) {

		LOGGER.warn(
				"serasa_response_invalid errorType={}",
				exception.getClass().getSimpleName()
		);

		return error(
				HttpStatus.BAD_GATEWAY,
				"SERASA_INVALID_RESPONSE",
				"Invalid response received from external credit service",
				Map.of(),
				request
		);
	}

	@ExceptionHandler(RestClientResponseException.class)
	ResponseEntity<ApiError> handleSerasaHttpError(
			RestClientResponseException exception,
			HttpServletRequest request) {

		int upstreamStatus = exception.getStatusCode().value();

		LOGGER.warn(
				"serasa_http_error upstreamStatus={} errorType={}",
				upstreamStatus,
				exception.getClass().getSimpleName()
		);

		if (upstreamStatus == 401 || upstreamStatus == 403) {
			return error(
					HttpStatus.BAD_GATEWAY,
					"SERASA_AUTH_ERROR",
					"External credit service authentication failed",
					Map.of(),
					request
			);
		}

		if (upstreamStatus == 429) {
			return error(
					HttpStatus.SERVICE_UNAVAILABLE,
					"SERASA_RATE_LIMIT",
					"External credit service is temporarily unavailable",
					Map.of(),
					request
			);
		}

		if (upstreamStatus == 503 || upstreamStatus == 504) {
			return error(
					HttpStatus.SERVICE_UNAVAILABLE,
					"SERASA_UNAVAILABLE",
					"External credit service is temporarily unavailable",
					Map.of(),
					request
			);
		}

		if (upstreamStatus >= 500) {
			return error(
					HttpStatus.BAD_GATEWAY,
					"SERASA_UPSTREAM_ERROR",
					"External credit service returned a technical error",
					Map.of(),
					request
			);
		}

		return error(
				HttpStatus.BAD_GATEWAY,
				"SERASA_REQUEST_REJECTED",
				"External credit service rejected the integration request",
				Map.of(),
				request
		);
	}

	@ExceptionHandler(ResourceAccessException.class)
	ResponseEntity<ApiError> handleSerasaConnectionError(
			ResourceAccessException exception,
			HttpServletRequest request) {

		boolean timeout = hasTimeoutCause(exception);

		LOGGER.warn(
				"serasa_connection_error timeout={} errorType={}",
				timeout,
				exception.getClass().getSimpleName()
		);

		if (timeout) {
			return error(
					HttpStatus.GATEWAY_TIMEOUT,
					"SERASA_TIMEOUT",
					"External credit service timed out; proposal outcome is unknown",
					Map.of(),
					request
			);
		}

		return error(
				HttpStatus.BAD_GATEWAY,
				"SERASA_CONNECTION_ERROR",
				"Unable to communicate with external credit service; proposal outcome may be unknown",
				Map.of(),
				request
		);
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ApiError> handleUnexpected(
			Exception exception,
			HttpServletRequest request) {

		LOGGER.error(
				"request_failed technicalResult=unexpected_error errorType={}",
				exception.getClass().getSimpleName()
		);

		return error(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"INTERNAL_ERROR",
				"An unexpected technical error occurred",
				Map.of(),
				request
		);
	}

	private boolean hasTimeoutCause(Throwable exception) {
		Throwable current = exception;

		while (current != null) {
			if (current instanceof SocketTimeoutException
					|| current instanceof HttpTimeoutException) {
				return true;
			}

			current = current.getCause();
		}

		return false;
	}

	private ResponseEntity<ApiError> error(
			HttpStatus status,
			String code,
			String message,
			Map<String, String> fieldErrors,
			HttpServletRequest request) {

		Object requestCorrelationId =
				request.getAttribute(
						CorrelationIdFilter.REQUEST_ATTRIBUTE
				);

		String correlationId =
				requestCorrelationId == null
						? CorrelationIdFilter.currentCorrelationId()
						: requestCorrelationId.toString();

		ApiError body = new ApiError(
				Instant.now(),
				status.value(),
				code,
				message,
				correlationId,
				fieldErrors
		);

		return ResponseEntity.status(status).body(body);
	}

	public record ApiError(
			Instant timestamp,
			int status,
			String code,
			String message,
			String correlationId,
			Map<String, String> fieldErrors
	) {
	}
}
