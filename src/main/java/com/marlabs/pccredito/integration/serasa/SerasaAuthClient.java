package com.marlabs.pccredito.integration.serasa;

import com.marlabs.pccredito.config.SerasaProperties;
import com.marlabs.pccredito.integration.serasa.dto.SerasaTokenRequest;
import com.marlabs.pccredito.integration.serasa.dto.SerasaTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SerasaAuthClient {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(SerasaAuthClient.class);

    private final RestClient restClient;
    private final SerasaProperties properties;

    public SerasaAuthClient(RestClient serasaRestClient, SerasaProperties properties) {
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

        long startedAt = System.nanoTime();

        try {

            LOGGER.info(
                    "serasa_auth_request " +
                            "method=POST " +
                            "url={} " +
                            "contentType={} " +
                            "accept={} " +
                            "userDomainPresent={} " +
                            "correlationIdPresent={} " +
                            "usernamePresent={} " +
                            "passwordPresent={} " +
                            "clientIdPresent={} " +
                            "clientSecretPresent={}",
                    properties.getTokenUrl(),
                    MediaType.APPLICATION_JSON_VALUE,
                    MediaType.APPLICATION_JSON_VALUE,
                    isConfigured(properties.getUserDomain()),
                    isConfigured(properties.getCorrelationId()),
                    isConfigured(properties.getUsername()),
                    isConfigured(properties.getPassword()),
                    isConfigured(properties.getClientId()),
                    isConfigured(properties.getClientSecret())
            );


            RestClient.RequestBodySpec requestSpec = restClient
                    .post()
                    .uri(properties.getTokenUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header(
                            "X-User-Domain",
                            properties.getUserDomain()
                    );

            if (properties.getCorrelationId() != null && !properties.getCorrelationId().isBlank()) {
                requestSpec.header("X-Correlation-Id",properties.getCorrelationId());
            }

            SerasaTokenResponse response = requestSpec
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

    private void validateConfiguration() {
        requireConfigured("SERASA_TOKEN_URL", properties.getTokenUrl());
        requireConfigured("SERASA_USERNAME", properties.getUsername());
        requireConfigured("SERASA_PASSWORD",properties.getPassword());
        requireConfigured("SERASA_CLIENT_ID",properties.getClientId());
        requireConfigured("SERASA_CLIENT_SECRET",properties.getClientSecret());
        requireConfigured("SERASA_USER_DOMAIN",properties.getUserDomain());
    }

    private static void requireConfigured(String name,String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Required Serasa configuration is missing: " + name
            );
        }
    }

    private static long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    private static boolean isConfigured(String value) {
        return value != null && !value.isBlank();
    }

}