package com.marlabs.pccredito.integration.serasa;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.marlabs.pccredito.config.SerasaProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

class SerasaAuthHttpRequestTest {

    private WireMockServer wireMock;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(0);
        wireMock.start();
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
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

        /*
         * Monte SerasaProperties aqui com VALORES FICTÍCIOS:
         *
         * tokenUrl =
         * http://localhost:<wiremock-port>/oauth2/experianone/v1/token
         *
         * username = test-user
         * password = test-password
         * clientId = test-client
         * clientSecret = test-secret
         * userDomain = veste.com
         */

        // execute authClient.requestToken()

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
