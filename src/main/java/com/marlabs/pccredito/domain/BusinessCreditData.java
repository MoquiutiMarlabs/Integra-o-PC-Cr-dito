package com.marlabs.pccredito.domain;

import java.math.BigDecimal;

/**
 * Isolates data whose origin and requiredness depend on SERASA-CONTRACT-001.
 */
public record BusinessCreditData(
		BigDecimal internalPunctuality,
		Integer averageDaysLate,
		BigDecimal amountDue,
		BigDecimal overdueAmount
) {
}

