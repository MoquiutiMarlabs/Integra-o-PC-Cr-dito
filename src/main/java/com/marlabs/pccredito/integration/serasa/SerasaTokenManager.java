package com.marlabs.pccredito.integration.serasa;

import org.springframework.stereotype.Component;

@Component
public class SerasaTokenManager {

	private final SerasaAuthClient authClient;

	public SerasaTokenManager(SerasaAuthClient authClient) {
		this.authClient = authClient;
	}

	public String getValidAccessToken() {
		// TODO: obtain and reuse a token in process memory only while it is valid.
		// Tokens, passwords and client secrets must never be persisted or logged.
		authClient.requestToken();
		throw new UnsupportedOperationException("Serasa token lifecycle is not enabled");
	}

	public void invalidate() {
		// TODO: invalidate the cached token after a Serasa 401.
	}

	/**
	 * TODO: when the HTTP contract is enabled, a 401 may trigger invalidation,
	 * token renewal and exactly one replay of the original call. Never loop.
	 */
	public int maximumReplayCountAfterUnauthorized() {
		return 1;
	}
}

