package com.marlabs.pccredito.integration.serasa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SerasaTokenRequest(
        String username,
        String password,
        @JsonProperty("client_id") String clientId,
        @JsonProperty("client_secret") String clientSecret
) {
    @Override
    public String toString() {
        return "SerasaTokenRequest[" +
                "username=***," +
                "password=***," +
                "clientId=***," +
                "clientSecret=***]";
    }
}
