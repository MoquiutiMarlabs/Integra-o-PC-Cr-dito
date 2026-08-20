package com.marlabs.pccredito.integration.serasa.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SerasaNovaPropostaRequest(
		@JsonProperty("DV-Application") Application application,
		@JsonProperty("DV-Applicant") ApplicantEnvelope applicant
) {

	public record Application(
			@JsonProperty("IDservico") String serviceId,
			@JsonProperty("Fonte") String source,
			@JsonProperty("ProdutoSolicitado") String requestedProduct,
			@JsonProperty("Produto") String product,
			@JsonProperty("Subproduto1") String subproduct1,
			@JsonProperty("Subproduto2") String subproduct2,
			@JsonProperty("ValorEmprestimoSolicitado") BigDecimal requestedLoanAmount,
			@JsonProperty("DadosEntradaPersonalizados") CustomInputData customInputData
	) {
	}

	public record CustomInputData(
			@JsonProperty("PontualidadeInterna") BigDecimal internalPunctuality,
			@JsonProperty("MediaDiasAtraso") Integer averageDaysLate,
			@JsonProperty("ValorAVencer") BigDecimal amountDue,
			@JsonProperty("ValorVencido") BigDecimal overdueAmount
	) {
	}

	public record ApplicantEnvelope(
			@JsonProperty("Applicant") Applicant applicant
	) {
	}

	public record Applicant(
			@JsonProperty("CNPJ") String cnpj
	) {
	}
}

