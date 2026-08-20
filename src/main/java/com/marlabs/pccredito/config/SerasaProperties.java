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
	private final Http http;

	public SerasaProperties(
			String baseUrl,
			String tokenUrl,
			String username,
			String password,
			String clientId,
			String clientSecret,
			Http http) {
		this.baseUrl = baseUrl;
		this.tokenUrl = tokenUrl;
		this.username = username;
		this.password = password;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.http = http;
	}

	public String getBaseUrl() {
		return baseUrl;
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

	public Http getHttp() {
		return http;
	}

	public record Http(Duration connectTimeout, Duration readTimeout) {
	}
}

