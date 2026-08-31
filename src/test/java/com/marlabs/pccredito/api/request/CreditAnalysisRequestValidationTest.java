package com.marlabs.pccredito.api.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import com.marlabs.pccredito.domain.CustomerRelationshipType;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class CreditAnalysisRequestValidationTest {

	private static final ValidatorFactory VALIDATOR_FACTORY =
			Validation.buildDefaultValidatorFactory();

	private static final Validator VALIDATOR =
			VALIDATOR_FACTORY.getValidator();

	@AfterAll
	static void closeValidatorFactory() {
		VALIDATOR_FACTORY.close();
	}

	@Test
	void shouldRejectCompletelyInvalidRequest() {

		CreditAnalysisRequest request =
				new CreditAnalysisRequest(
						null,
						null,
						null,
						null,
						null,
						null,
						null
				);

		Set<ConstraintViolation<CreditAnalysisRequest>> violations =
				VALIDATOR.validate(request);

		assertTrue(hasViolationFor(violations, "cnpj"));
		assertTrue(hasViolationFor(violations, "subproduto2"));
		assertTrue(hasViolationFor(violations, "valorSolicitado"));
	}

	@Test
	void shouldRejectRequestedAmountEqualToOrLessThanZero() {

		CreditAnalysisRequest zeroRequest =
				new CreditAnalysisRequest(
						"12345678000199",
						CustomerRelationshipType.NOVO,
						0L,
						null,
						null,
						null,
						null
				);

		CreditAnalysisRequest negativeRequest =
				new CreditAnalysisRequest(
						"12345678000199",
						CustomerRelationshipType.NOVO,
						-1L,
						null,
						null,
						null,
						null
				);

		Set<ConstraintViolation<CreditAnalysisRequest>> zeroViolations =
				VALIDATOR.validate(zeroRequest);

		Set<ConstraintViolation<CreditAnalysisRequest>> negativeViolations =
				VALIDATOR.validate(negativeRequest);

		assertTrue(
				hasViolationFor(
						zeroViolations,
						"valorSolicitado"
				)
		);

		assertTrue(
				hasViolationFor(
						negativeViolations,
						"valorSolicitado"
				)
		);
	}

	@Test
	void shouldRejectMissingCnpj() {

		CreditAnalysisRequest request =
				new CreditAnalysisRequest(
						" ",
						CustomerRelationshipType.NOVO,
						10000L,
						null,
						null,
						null,
						null
				);

		Set<ConstraintViolation<CreditAnalysisRequest>> violations =
				VALIDATOR.validate(request);

		assertTrue(hasViolationFor(violations, "cnpj"));
	}

	@Test
	void shouldRejectInvalidCnpjFormat() {

		CreditAnalysisRequest request =
				new CreditAnalysisRequest(
						"1234",
						CustomerRelationshipType.NOVO,
						10000L,
						null,
						null,
						null,
						null
				);

		Set<ConstraintViolation<CreditAnalysisRequest>> violations =
				VALIDATOR.validate(request);

		assertTrue(hasViolationFor(violations, "cnpj"));
	}

	@Test
	void shouldRejectInternalPunctualityBelowZero() {

		CreditAnalysisRequest request =
				new CreditAnalysisRequest(
						"12345678000199",
						CustomerRelationshipType.NOVO,
						10000L,
						new BigDecimal("-0.01"),
						null,
						null,
						null
				);

		Set<ConstraintViolation<CreditAnalysisRequest>> violations =
				VALIDATOR.validate(request);

		assertTrue(
				hasViolationFor(
						violations,
						"pontualidadeInterna"
				)
		);
	}

	@Test
	void shouldRejectInternalPunctualityAboveOneHundred() {

		CreditAnalysisRequest request =
				new CreditAnalysisRequest(
						"12345678000199",
						CustomerRelationshipType.NOVO,
						10000L,
						new BigDecimal("100.01"),
						null,
						null,
						null
				);

		Set<ConstraintViolation<CreditAnalysisRequest>> violations =
				VALIDATOR.validate(request);

		assertTrue(
				hasViolationFor(
						violations,
						"pontualidadeInterna"
				)
		);
	}

	@Test
	void shouldAcceptOptionalBusinessFieldsWhenAbsent() {

		CreditAnalysisRequest request =
				new CreditAnalysisRequest(
						"12345678000199",
						CustomerRelationshipType.CARTEIRA,
						10000L,
						null,
						null,
						null,
						null
				);

		Set<ConstraintViolation<CreditAnalysisRequest>> violations =
				VALIDATOR.validate(request);

		assertEquals(0, violations.size());
	}

	@Test
	void shouldAcceptCompleteValidRequest() {

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

		Set<ConstraintViolation<CreditAnalysisRequest>> violations =
				VALIDATOR.validate(request);

		assertEquals(0, violations.size());
	}

	private boolean hasViolationFor(
			Set<ConstraintViolation<CreditAnalysisRequest>> violations,
			String property) {

		return violations.stream()
				.anyMatch(
						violation ->
								violation
										.getPropertyPath()
										.toString()
										.equals(property)
				);
	}
}