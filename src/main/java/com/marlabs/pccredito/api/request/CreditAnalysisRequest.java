package com.marlabs.pccredito.api.request;

import java.math.BigDecimal;

import com.marlabs.pccredito.domain.CustomerRelationshipType;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record CreditAnalysisRequest(

		@NotBlank(message = "cnpj is required")
		@Pattern(
				regexp = "\\d{14}",
				message = "cnpj must contain exactly 14 digits"
		)
		String cnpj,

		@NotNull(message = "subproduto2 is required")
		CustomerRelationshipType subproduto2,

		@NotNull(message = "valorSolicitado is required")
		@Positive(message = "valorSolicitado must be greater than zero")
		Long valorSolicitado,

		@DecimalMin(
				value = "0",
				message = "pontualidadeInterna must be greater than or equal to 0"
		)
		@DecimalMax(
				value = "100",
				message = "pontualidadeInterna must be less than or equal to 100"
		)
		BigDecimal pontualidadeInterna,

		@Min(
				value = 0,
				message = "mediaDiasAtraso must be greater than or equal to 0"
		)
		Integer mediaDiasAtraso,

		@DecimalMin(
				value = "0",
				message = "valorAVencer must be greater than or equal to 0"
		)
		BigDecimal valorAVencer,

		@DecimalMin(
				value = "0",
				message = "valorVencido must be greater than or equal to 0"
		)
		BigDecimal valorVencido

) {
}