package com.marlabs.pccredito.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.env.Environment;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class IpWhitelistFilter extends OncePerRequestFilter {

	private final boolean enabled;
	private final Set<String> allowedAddresses;

	public IpWhitelistFilter(Environment environment) {
		this.enabled = environment.getProperty("security.ip-whitelist.enabled", Boolean.class, false);
		String configuredAddresses = environment.getProperty("security.ip-whitelist.allowed-addresses", "");
		this.allowedAddresses = Arrays.stream(configuredAddresses.split(","))
				.map(String::trim)
				.filter(address -> !address.isEmpty())
				.collect(Collectors.toUnmodifiableSet());
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		if (!enabled || allowedAddresses.contains(request.getRemoteAddr())) {
			filterChain.doFilter(request, response);
			return;
		}

		response.sendError(HttpServletResponse.SC_FORBIDDEN);
	}
}
