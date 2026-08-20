package com.marlabs.pccredito.api.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreditAnalysisRequest(
		@NotBlank(message = "cnpj is required") String cnpj,
		@NotNull(message = "valorSolicitado is required")
		@Positive(message = "valorSolicitado must be greater than zero") BigDecimal valorSolicitado
) {
}

