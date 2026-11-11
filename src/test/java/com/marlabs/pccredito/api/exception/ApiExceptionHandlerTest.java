package com.marlabs.pccredito.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.SocketTimeoutException;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler =
            new ApiExceptionHandler();

    private final MockHttpServletRequest request =
            new MockHttpServletRequest();

    @Test
    void shouldMapSerasaUnauthorizedToBadGateway() {

        var exception =
                new HttpClientErrorException(
                        HttpStatus.UNAUTHORIZED
                );

        var response =
                handler.handleSerasaHttpError(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.BAD_GATEWAY,
                response.getStatusCode()
        );

        assertEquals(
                "SERASA_AUTH_ERROR",
                response.getBody().code()
        );
    }

    @Test
    void shouldMapRateLimitToServiceUnavailable() {

        var exception =
                new HttpClientErrorException(
                        HttpStatus.TOO_MANY_REQUESTS
                );

        var response =
                handler.handleSerasaHttpError(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.SERVICE_UNAVAILABLE,
                response.getStatusCode()
        );

        assertEquals(
                "SERASA_RATE_LIMIT",
                response.getBody().code()
        );
    }

    @Test
    void shouldMapSerasaServerErrorToBadGateway() {

        var exception =
                new HttpServerErrorException(
                        HttpStatus.INTERNAL_SERVER_ERROR
                );

        var response =
                handler.handleSerasaHttpError(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.BAD_GATEWAY,
                response.getStatusCode()
        );
    }

    @Test
    void shouldMapTimeoutToGatewayTimeout() {

        var exception = new ResourceAccessException(
                "Timeout",
                new SocketTimeoutException()
        );

        var response =
                handler.handleSerasaConnectionError(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.GATEWAY_TIMEOUT,
                response.getStatusCode()
        );

        assertEquals(
                "SERASA_TIMEOUT",
                response.getBody().code()
        );

        assertTrue(
                response.getBody()
                        .message()
                        .contains("unknown")
        );
    }

    @Test
    void shouldMapConnectionFailureToBadGateway() {

        var exception =
                new ResourceAccessException(
                        "Connection refused"
                );

        var response =
                handler.handleSerasaConnectionError(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.BAD_GATEWAY,
                response.getStatusCode()
        );

        assertEquals(
                "SERASA_CONNECTION_ERROR",
                response.getBody().code()
        );
    }
}
