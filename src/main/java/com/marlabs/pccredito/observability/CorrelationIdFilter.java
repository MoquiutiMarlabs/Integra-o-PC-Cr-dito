package com.marlabs.pccredito.observability;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

	public static final String HEADER_NAME = "X-Correlation-Id";
	public static final String MDC_KEY = "correlationId";
	public static final String REQUEST_ATTRIBUTE = CorrelationIdFilter.class.getName() + ".correlationId";

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String incomingCorrelationId = request.getHeader(HEADER_NAME);
		String correlationId = StringUtils.hasText(incomingCorrelationId)
				? incomingCorrelationId
				: UUID.randomUUID().toString();

		request.setAttribute(REQUEST_ATTRIBUTE, correlationId);
		response.setHeader(HEADER_NAME, correlationId);
		MDC.put(MDC_KEY, correlationId);
		try {
			filterChain.doFilter(request, response);
		}
		finally {
			MDC.remove(MDC_KEY);
		}
	}

	public static String currentCorrelationId() {
		return MDC.get(MDC_KEY);
	}
}

