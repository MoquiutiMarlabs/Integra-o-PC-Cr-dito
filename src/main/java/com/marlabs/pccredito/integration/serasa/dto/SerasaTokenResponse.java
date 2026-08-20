package com.marlabs.pccredito.integration.serasa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// TODO: validate the exact OAuth response fields against the formal Serasa auth contract.
public record SerasaTokenResponse(
		@JsonProperty("access_token") String accessToken,
		@JsonProperty("token_type") String tokenType,
		@JsonProperty("expires_in") Long expiresIn
) {
}

