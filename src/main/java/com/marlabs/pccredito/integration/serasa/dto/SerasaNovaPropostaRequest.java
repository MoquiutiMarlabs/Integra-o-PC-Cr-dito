package com.marlabs.pccredito.integration.serasa.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SerasaNovaPropostaRequest(

		@JsonProperty("DV-Application")
		Application application,

		@JsonProperty("DV-Applicant")
		ApplicantContainer applicant

) {

	public record Application(

			@JsonProperty("IDservico")
			String idServico,

			@JsonProperty("Fonte")
			String fonte,

			@JsonProperty("ProdutoSolicitado")
			ProdutoSolicitado produtoSolicitado,

			@JsonProperty("DadosEntradaPersonalizados")
			List<DadoEntradaPersonalizado> dadosEntradaPersonalizados
	) {
	}

	public record ProdutoSolicitado(

			@JsonProperty("Produto")
			String produto,

			@JsonProperty("Subproduto1")
			String subproduto1,

			@JsonProperty("Subproduto2")
			String subproduto2,

			@JsonProperty("ValorEmprestimoSolicitado")
			Long valorEmprestimoSolicitado
	) {
	}

	public record DadoEntradaPersonalizado(

			@JsonProperty("Chave")
			String chave,

			@JsonProperty("Valor")
			String valor
	) {
	}

	public record ApplicantContainer(

			@JsonProperty("Applicant")
			List<Applicant> applicants
	) {
	}

	public record Applicant(

			@JsonProperty("CNPJ")
			String cnpj
	) {
	}
}