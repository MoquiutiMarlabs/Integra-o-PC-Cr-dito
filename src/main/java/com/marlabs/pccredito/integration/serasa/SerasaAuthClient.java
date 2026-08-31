package com.marlabs.pccredito.integration.serasa;

import com.marlabs.pccredito.config.SerasaProperties;
import com.marlabs.pccredito.integration.serasa.dto.SerasaTokenRequest;
import com.marlabs.pccredito.integration.serasa.dto.SerasaTokenResponse;
import com.marlabs.pccredito.observability.CorrelationIdFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class SerasaAuthClient {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(SerasaAuthClient.class);

    private final RestClient restClient;
    private final SerasaProperties properties;

    public SerasaAuthClient(
            RestClient serasaRestClient,
            SerasaProperties properties) {

        this.restClient = serasaRestClient;
        this.properties = properties;
    }

    public SerasaTokenResponse requestToken() {

        validateConfiguration();

        SerasaTokenRequest request = new SerasaTokenRequest(
                properties.getUsername(),
                properties.getPassword(),
                properties.getClientId(),
                properties.getClientSecret()
        );

        String serasaCorrelationId =
                resolveSerasaCorrelationId(
                        CorrelationIdFilter.currentCorrelationId()
                );

        long startedAt = System.nanoTime();

        LOGGER.info(
                "serasa_auth_started operation=serasa-auth"
        );

        try {

            SerasaTokenResponse response = restClient
                    .post()
                    .uri(properties.getTokenUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header(
                            "X-User-Domain",
                            properties.getUserDomain()
                    )
                    .header(
                            "X-Correlation-Id",
                            serasaCorrelationId
                    )
                    .body(request)
                    .retrieve()
                    .body(SerasaTokenResponse.class);

            if (response == null
                    || response.accessToken() == null
                    || response.accessToken().isBlank()) {

                throw new IllegalStateException(
                        "Serasa authentication returned no access token"
                );
            }

            LOGGER.info(
                    "serasa_auth_finished " +
                            "operation=serasa-auth " +
                            "technicalResult=success " +
                            "durationMs={}",
                    elapsedMillis(startedAt)
            );

            return response;

        } catch (RuntimeException exception) {

            LOGGER.warn(
                    "serasa_auth_finished " +
                            "operation=serasa-auth " +
                            "technicalResult=error " +
                            "durationMs={} " +
                            "errorType={}",
                    elapsedMillis(startedAt),
                    exception.getClass().getSimpleName()
            );

            throw exception;
        }
    }

    private String resolveSerasaCorrelationId(
            String currentCorrelationId) {

        if (currentCorrelationId != null
                && !currentCorrelationId.isBlank()) {

            try {
                UUID.fromString(currentCorrelationId);
                return currentCorrelationId;
            } catch (IllegalArgumentException ignored) {
                // Serasa exige UUID válido.
            }
        }

        return UUID.randomUUID().toString();
    }

    private void validateConfiguration() {

        requireConfigured(
                "SERASA_TOKEN_URL",
                properties.getTokenUrl()
        );

        requireConfigured(
                "SERASA_USERNAME",
                properties.getUsername()
        );

        requireConfigured(
                "SERASA_PASSWORD",
                properties.getPassword()
        );

        requireConfigured(
                "SERASA_CLIENT_ID",
                properties.getClientId()
        );

        requireConfigured(
                "SERASA_CLIENT_SECRET",
                properties.getClientSecret()
        );

        requireConfigured(
                "SERASA_USER_DOMAIN",
                properties.getUserDomain()
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