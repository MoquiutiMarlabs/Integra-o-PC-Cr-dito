package com.marlabs.pccredito.config;

import java.net.http.HttpClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.marlabs.pccredito.observability.CorrelationIdFilter;

@Configuration
public class HttpClientConfiguration {

	@Bean
	RestClient serasaRestClient(SerasaProperties properties) {
		HttpClient httpClient = HttpClient.newBuilder()
				.connectTimeout(properties.getHttp().connectTimeout())
				.build();
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(properties.getHttp().readTimeout());

		RestClient.Builder clientBuilder = RestClient.builder()
				.requestFactory(requestFactory)
				.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.requestInterceptor((request, body, execution) -> {
					String correlationId = CorrelationIdFilter.currentCorrelationId();
					if (correlationId != null) {
						request.getHeaders().set(CorrelationIdFilter.HEADER_NAME, correlationId);
					}
					return execution.execute(request, body);
				});

		if (properties.getBaseUrl() != null && !properties.getBaseUrl().isBlank()) {
			clientBuilder.baseUrl(properties.getBaseUrl());
		}
		return clientBuilder.build();
	}
}

