package com.marlabs.pccredito.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "serasa")
public class SerasaProperties {

	private final String baseUrl;
	private final String tokenUrl;
	private final String username;
	private final String password;
	private final String clientId;
	private final String clientSecret;
	private final String userDomain;
	private final String correlationId;
	private final Http http;
	private final String fonte;
	private final String novaPropostaPath;

	public SerasaProperties(
			String baseUrl,
			String tokenUrl,
			String username,
			String password,
			String clientId,
			String clientSecret,
			String userDomain,
			String correlationId,
			Http http,
			String fonte,
			String novaPropostaPath) {
		this.baseUrl = baseUrl;
		this.tokenUrl = tokenUrl;
		this.username = username;
		this.password = password;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.userDomain = userDomain;
		this.correlationId = correlationId;
		this.http = http;
		this.fonte = fonte;
		this.novaPropostaPath = novaPropostaPath;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public String getFonte() {
		return fonte;
	}

	public String getNovaPropostaPath() {
		return novaPropostaPath;
	}

	public String getTokenUrl() {
		return tokenUrl;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getClientId() {
		return clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public String getUserDomain() {
		return userDomain;
	}

	public Http getHttp() {
		return http;
	}

	public record Http(Duration connectTimeout, Duration readTimeout) {
	}
}

