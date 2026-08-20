package com.marlabs.pccredito.api.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class CreditAnalysisRequestValidationTest {

	private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
	private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

	@AfterAll
	static void closeValidatorFactory() {
		VALIDATOR_FACTORY.close();
	}

	@Test
	void shouldRejectCompletelyInvalidRequest() {
		Set<ConstraintViolation<CreditAnalysisRequest>> violations =
				VALIDATOR.validate(new CreditAnalysisRequest(null, null));

		assertEquals(2, violations.size());
	}

	@Test
	void shouldRejectRequestedAmountEqualToOrLessThanZero() {
		Set<ConstraintViolation<CreditAnalysisRequest>> zeroViolations =
				VALIDATOR.validate(new CreditAnalysisRequest("12345678000199", BigDecimal.ZERO));
		Set<ConstraintViolation<CreditAnalysisRequest>> negativeViolations =
				VALIDATOR.validate(new CreditAnalysisRequest("12345678000199", BigDecimal.valueOf(-1)));

		assertTrue(hasViolationFor(zeroViolations, "valorSolicitado"));
		assertTrue(hasViolationFor(negativeViolations, "valorSolicitado"));
	}

	@Test
	void shouldRejectMissingCnpj() {
		Set<ConstraintViolation<CreditAnalysisRequest>> violations =
				VALIDATOR.validate(new CreditAnalysisRequest(" ", BigDecimal.ONE));

		assertTrue(hasViolationFor(violations, "cnpj"));
	}

	private boolean hasViolationFor(
			Set<ConstraintViolation<CreditAnalysisRequest>> violations,
			String property) {
		return violations.stream()
				.anyMatch(violation -> violation.getPropertyPath().toString().equals(property));
	}
}
