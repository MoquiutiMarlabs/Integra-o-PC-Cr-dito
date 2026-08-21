package com.marlabs.pccredito.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
public class SecurityConfiguration {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, Environment environment) throws Exception {
		IpWhitelistFilter ipWhitelistFilter = new IpWhitelistFilter(environment);
		boolean authenticationRequired = environment.getProperty(
				"security.m2m.authentication-required", Boolean.class, true);

		// TODO: configure Veste-to-Marlabs OAuth2 Client Credentials/JWT after
		// issuer, audience and claims are formally defined. Never reuse that
		// credential for Marlabs-to-Serasa calls.
		return http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.httpBasic(httpBasic -> httpBasic.disable())
				.formLogin(formLogin -> formLogin.disable())
				.logout(logout -> logout.disable())
				.addFilterBefore(ipWhitelistFilter, AnonymousAuthenticationFilter.class)
				.authorizeHttpRequests(authorize -> {
					authorize.requestMatchers("/actuator/health", "/actuator/health/**", "/internal/dev/serasa/auth-check").permitAll();
					if (authenticationRequired) {
						authorize.anyRequest().denyAll();
					}
					else {
						authorize.requestMatchers("/v1/credit-analyses").permitAll()
							.anyRequest().denyAll();
					}
				})
				.build();
	}
}

