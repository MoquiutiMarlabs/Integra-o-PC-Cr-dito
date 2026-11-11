package com.marlabs.pccredito.integration.serasa;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.marlabs.pccredito.api.response.CreditAnalysisResponse;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class SerasaResponseMapper {

    private final ObjectMapper objectMapper;

    public SerasaResponseMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CreditAnalysisResponse toVesteResponse(
            String requestId,
            String responseBody) {

        if (responseBody == null || responseBody.isBlank()) {
            throw new InvalidSerasaResponseException(
                    "Serasa returned an empty response"
            );
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);

            JsonNode data = root.path("data");

            JsonNode application = data.path("DV-Application");

            if (!application.isObject()) {
                throw new InvalidSerasaResponseException(
                        "Serasa response does not contain DV-Application"
                );
            }

            JsonNode results = data
                    .path("DV-Results")
                    .path("MergedResults");

            JsonNode firstResult =
                    results.isArray() && !results.isEmpty()
                            ? results.get(0)
                            : null;

            JsonNode decision = firstResult == null
                    ? null
                    : firstResult.path("ResultadoDecisao");

            JsonNode applicationResults =
                    application.path("ApplicationResults");

            BigDecimal limiteSugerido =
                    decimalOrNull(
                            applicationResults.path(
                                    "ValorCreditoRecomendado"
                            )
                    );

            var serasaResult =
                    new CreditAnalysisResponse.SerasaResult(
                            textOrNull(
                                    application.path("NumeroProposta")
                            ),
                            textOrNull(
                                    application.path(
                                            "CodigoRecomendacaoProposta"
                                    )
                            ),
                            textOrNull(
                                    application.path(
                                            "NomeRecomendacaoProposta"
                                    )
                            ),
                            decision == null
                                    ? null
                                    : textOrNull(
                                    decision.path("MensagemDecisao")
                            ),
                            decision == null
                                    ? null
                                    : textOrNull(
                                    decision.path(
                                            "ClassificacaoDecisao"
                                    )
                            ),
                            limiteSugerido
                    );

            return new CreditAnalysisResponse(
                    requestId,
                    serasaResult
            );

        } catch (InvalidSerasaResponseException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InvalidSerasaResponseException(
                    "Unable to map Serasa response",
                    exception
            );
        }
    }

    private String textOrNull(JsonNode node) {
        if (node == null || node.isMissingNode()
                || node.isNull() || !node.isValueNode()) {
            return null;
        }

        return node.asText();
    }

    private BigDecimal decimalOrNull(JsonNode node) {
        String value = textOrNull(node);

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            throw new InvalidSerasaResponseException(
                    "Invalid ValorCreditoRecomendado",
                    exception
            );
        }
    }
}
