package com.marlabs.pccredito.observability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

	private final CorrelationIdFilter filter = new CorrelationIdFilter();

	@Test
	void shouldPreserveIncomingCorrelationId() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.addHeader(CorrelationIdFilter.HEADER_NAME, "veste-correlation-123");

		filter.doFilter(request, response, new MockFilterChain());

		assertEquals("veste-correlation-123", response.getHeader(CorrelationIdFilter.HEADER_NAME));
		assertEquals("veste-correlation-123", request.getAttribute(CorrelationIdFilter.REQUEST_ATTRIBUTE));
		assertNull(CorrelationIdFilter.currentCorrelationId());
	}

	@Test
	void shouldGenerateCorrelationIdWhenHeaderIsMissing() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, new MockFilterChain());

		String generatedCorrelationId = response.getHeader(CorrelationIdFilter.HEADER_NAME);
		assertEquals(generatedCorrelationId, UUID.fromString(generatedCorrelationId).toString());
		assertEquals(generatedCorrelationId, request.getAttribute(CorrelationIdFilter.REQUEST_ATTRIBUTE));
	}
}
