package com.marlabs.pccredito.integration.serasa;

import com.marlabs.pccredito.application.port.SerasaCreditGateway;
import com.marlabs.pccredito.config.SerasaProperties;
import com.marlabs.pccredito.domain.CreditAnalysis;
import com.marlabs.pccredito.integration.serasa.dto.SerasaNovaPropostaRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SerasaCreditClient implements SerasaCreditGateway {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(SerasaCreditClient.class);

    private final RestClient restClient;
    private final SerasaRequestMapper requestMapper;
    private final SerasaProperties properties;

    public SerasaCreditClient(
            RestClient serasaRestClient,
            SerasaRequestMapper requestMapper,
            SerasaProperties properties) {

        this.restClient = serasaRestClient;
        this.requestMapper = requestMapper;
        this.properties = properties;
    }

    @Override
    public String submit(
            CreditAnalysis analysis,
            String idempotencyKey,
            String accessToken) {

        validateConfiguration();

        SerasaNovaPropostaRequest request =
                requestMapper.toNovaProposta(
                        analysis,
                        properties.getFonte()
                );

        long startedAt = System.nanoTime();

        LOGGER.info(
                "serasa_credit_started requestId={} operation=NovaProposta",
                analysis.requestId()
        );

        try {

            ResponseEntity<String> response = restClient
                    .post()
                    .uri(properties.getNovaPropostaPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + accessToken
                    )
                    .header(
                            "X-Screenless-Kill-Null",
                            "true"
                    )
                    .body(request)
                    .retrieve()
                    .toEntity(String.class);

            LOGGER.info(
                    "serasa_credit_finished requestId={} operation=NovaProposta technicalResult=success httpStatus={} durationMs={}",
                    analysis.requestId(),
                    response.getStatusCode().value(),
                    elapsedMillis(startedAt)
            );

            String responseBody = response.getBody();

            if (responseBody == null || responseBody.isBlank()) {
                throw new IllegalStateException(
                        "Serasa NovaProposta returned an empty response body"
                );
            }

            return responseBody;

        } catch (RuntimeException exception) {

            LOGGER.warn(
                    "serasa_credit_finished requestId={} operation=NovaProposta technicalResult=error durationMs={} errorType={}",
                    analysis.requestId(),
                    elapsedMillis(startedAt),
                    exception.getClass().getSimpleName()
            );

            throw exception;
        }
    }

    private void validateConfiguration() {

        requireConfigured(
                "SERASA_NOVA_PROPOSTA_PATH",
                properties.getNovaPropostaPath()
        );

        requireConfigured(
                "SERASA_FONTE",
                properties.getFonte()
        );
    }

    private static void requireConfigured(
            String name,
            String value) {

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Required Serasa configuration is missing: " + name
            );
        }
    }

    private static long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}