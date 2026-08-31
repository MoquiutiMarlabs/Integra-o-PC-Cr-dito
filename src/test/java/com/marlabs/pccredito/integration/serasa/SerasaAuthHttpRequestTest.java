package com.marlabs.pccredito.integration.serasa;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.marlabs.pccredito.config.SerasaProperties;
import com.marlabs.pccredito.integration.serasa.dto.SerasaTokenResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

class SerasaAuthHttpRequestTest {

    private WireMockServer wireMock;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(0);
        wireMock.start();
    }

    @AfterEach
    void tearDown() {
        if (wireMock != null && wireMock.isRunning()) {
            wireMock.stop();
        }
    }

    @Test
    void shouldSendExpectedSerasaAuthenticationRequest() {

        wireMock.stubFor(
                post(urlEqualTo("/oauth2/experianone/v1/token"))
                        .willReturn(
                                okJson("""
                                {
                                  "access_token": "fake-token",
                                  "token_type": "Bearer",
                                  "expires_in": 3600
                                }
                                """)
                        )
        );

        SerasaProperties properties = new SerasaProperties(
                wireMock.baseUrl(),
                wireMock.baseUrl() + "/oauth2/experianone/v1/token",
                "test-user",
                "test-password",
                "test-client",
                "test-secret",
                "veste.com",
                new SerasaProperties.Http(Duration.ofSeconds(1), Duration.ofSeconds(2)),
                null,
                null
        );
        java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
                .version(java.net.http.HttpClient.Version.HTTP_1_1)
                .build();
        RestClient restClient = RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
        SerasaAuthClient authClient = new SerasaAuthClient(restClient, properties);

        SerasaTokenResponse token = authClient.requestToken();

        assertEquals("fake-token", token.accessToken());
        assertEquals("Bearer", token.tokenType());
        assertEquals(3600L, token.expiresIn());

        wireMock.verify(
                postRequestedFor(
                        urlEqualTo("/oauth2/experianone/v1/token")
                )
                        .withHeader(
                                "Content-Type",
                                containing("application/json")
                        )
                        .withHeader(
                                "Accept",
                                containing("application/json")
                        )
                        .withHeader(
                                "X-User-Domain",
                                equalTo("veste.com")
                        )
                        .withRequestBody(
                                matchingJsonPath(
                                        "$.username",
                                        equalTo("test-user")
                                )
                        )
                        .withRequestBody(
                                matchingJsonPath(
                                        "$.password",
                                        equalTo("test-password")
                                )
                        )
                        .withRequestBody(
                                matchingJsonPath(
                                        "$.client_id",
                                        equalTo("test-client")
                                )
                        )
                        .withRequestBody(
                                matchingJsonPath(
                                        "$.client_secret",
                                        equalTo("test-secret")
                                )
                        )
        );
    }
}
