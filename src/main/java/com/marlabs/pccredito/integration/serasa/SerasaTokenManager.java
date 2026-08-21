package com.marlabs.pccredito.integration.serasa;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.marlabs.pccredito.integration.serasa.dto.SerasaTokenResponse;

@Component
public class SerasaTokenManager {

	private static final long EXPIRATION_SAFETY_MARGIN_SECONDS = 30;

	private final SerasaAuthClient authClient;

	private volatile String accessToken;
	private volatile Instant expiresAt;

	public SerasaTokenManager(SerasaAuthClient authClient) {
		this.authClient = authClient;
	}

	public String getValidAccessToken() {

		if (isCachedTokenValid()) {
			return accessToken;
		}

		synchronized (this) {

			if (isCachedTokenValid()) {
				return accessToken;
			}

			SerasaTokenResponse response = authClient.requestToken();

			accessToken = response.accessToken();

			Long expiresIn = response.expiresIn();

			if (expiresIn != null && expiresIn > 0) {

				long safeLifetime = Math.max(
						1,
						expiresIn - EXPIRATION_SAFETY_MARGIN_SECONDS
				);

				expiresAt = Instant.now().plusSeconds(safeLifetime);

			} else {

				/*
				 * TODO SERASA-AUTH-001:
				 * Confirmar formalmente expires_in no contrato Serasa.
				 *
				 * Sem validade conhecida, o token não será considerado
				 * reutilizável.
				 */
				expiresAt = null;
			}

			return accessToken;
		}
	}

	public synchronized void invalidate() {
		accessToken = null;
		expiresAt = null;
	}

	private boolean isCachedTokenValid() {
		return accessToken != null
				&& expiresAt != null
				&& Instant.now().isBefore(expiresAt);
	}

	public int maximumReplayCountAfterUnauthorized() {
		return 1;
	}
}