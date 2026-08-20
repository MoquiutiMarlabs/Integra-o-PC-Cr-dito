package com.marlabs.pccredito.integration.serasa;

import org.springframework.stereotype.Component;

import com.marlabs.pccredito.integration.serasa.dto.SerasaTokenResponse;

@Component
public class SerasaAuthClient {

	public SerasaTokenResponse requestToken() {
		// TODO: implement only after the Serasa authentication request contract is formalized.
		throw new UnsupportedOperationException("Serasa authentication integration is not enabled");
	}
}

