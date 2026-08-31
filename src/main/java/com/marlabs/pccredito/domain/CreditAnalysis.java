package com.marlabs.pccredito.domain;

import java.math.BigDecimal;

public record CreditAnalysis(

		String requestId,

		String cnpj,

		CustomerRelationshipType customerRelationshipType,

		Long requestedAmount,

		BigDecimal internalPunctuality,

		Integer averageDelayDays,

		BigDecimal amountDue,

		BigDecimal overdueAmount

) {

	public CreditAnalysis {

		internalPunctuality =
				internalPunctuality == null
						? BigDecimal.ZERO
						: internalPunctuality;

		averageDelayDays =
				averageDelayDays == null
						? 0
						: averageDelayDays;

		amountDue =
				amountDue == null
						? BigDecimal.ZERO
						: amountDue;

		overdueAmount =
				overdueAmount == null
						? BigDecimal.ZERO
						: overdueAmount;
	}
}