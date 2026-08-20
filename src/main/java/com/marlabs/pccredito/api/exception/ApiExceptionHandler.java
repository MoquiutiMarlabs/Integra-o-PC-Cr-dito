package com.marlabs.pccredito.api.exception;

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

import com.marlabs.pccredito.integration.serasa.PendingSerasaContractException;
import com.marlabs.pccredito.observability.CorrelationIdFilter;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ApiExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiError> handleValidation(
			MethodArgumentNotValidException exception,
			HttpServletRequest request) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors().forEach(error ->
				fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));

		return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed",
				fieldErrors, request);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<ApiError> handleUnreadableMessage(
			HttpMessageNotReadableException exception,
			HttpServletRequest request) {
		return error(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Request body is malformed",
				Map.of(), request);
	}

	@ExceptionHandler(PendingSerasaContractException.class)
	ResponseEntity<ApiError> handlePendingContract(
			PendingSerasaContractException exception,
			HttpServletRequest request) {
		LOGGER.warn("request_failed technicalResult=pending_contract contractId={}",
				PendingSerasaContractException.CONTRACT_ID);
		return error(HttpStatus.SERVICE_UNAVAILABLE, PendingSerasaContractException.CONTRACT_ID,
				"External contract confirmation is pending", Map.of(), request);
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ApiError> handleUnexpected(Exception exception, HttpServletRequest request) {
		LOGGER.error("request_failed technicalResult=unexpected_error errorType={}",
				exception.getClass().getSimpleName());
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
				"An unexpected technical error occurred", Map.of(), request);
	}

	private ResponseEntity<ApiError> error(
			HttpStatus status,
			String code,
			String message,
			Map<String, String> fieldErrors,
			HttpServletRequest request) {
		Object requestCorrelationId = request.getAttribute(CorrelationIdFilter.REQUEST_ATTRIBUTE);
		String correlationId = requestCorrelationId == null
				? CorrelationIdFilter.currentCorrelationId()
				: requestCorrelationId.toString();
		ApiError body = new ApiError(Instant.now(), status.value(), code, message, correlationId, fieldErrors);
		return ResponseEntity.status(status).body(body);
	}

	public record ApiError(
			Instant timestamp,
			int status,
			String code,
			String message,
			String correlationId,
			Map<String, String> fieldErrors) {
	}
}

