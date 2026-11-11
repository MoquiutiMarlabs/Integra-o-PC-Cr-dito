package com.marlabs.pccredito.api.response;

import java.math.BigDecimal;

public record CreditAnalysisResponse(
		String requestId,
		SerasaResult serasa
) {
	public record SerasaResult(
			String numeroProposta,
			String codigoRecomendacao,
			String nomeRecomendacao,
			String mensagemDecisao,
			String classificacaoDecisao,
			BigDecimal limiteSugerido
	) {
	}
}
