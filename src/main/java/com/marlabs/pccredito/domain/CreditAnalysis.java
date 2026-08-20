package com.marlabs.pccredito.domain;

import java.math.BigDecimal;

public record CreditAnalysis(
		String requestId,
		String cnpj,
		BigDecimal requestedAmount
) {
}

