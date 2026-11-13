package com.marlabs.pccredito.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
public class SecurityConfiguration {

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			Environment environment) throws Exception {

		validateGatewayConfiguration(environment);

		boolean dev = environment.acceptsProfiles(
				Profiles.of("dev")
		);

		return http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session ->
						session.sessionCreationPolicy(
								SessionCreationPolicy.STATELESS
						)
				)
				.httpBasic(basic -> basic.disable())
				.formLogin(login -> login.disable())
				.logout(logout -> logout.disable())
				.addFilterBefore(
						new IpWhitelistFilter(environment),
						AnonymousAuthenticationFilter.class
				)
				.authorizeHttpRequests(authorize -> {
					authorize.requestMatchers(
							"/actuator/health",
							"/actuator/health/**"
					).permitAll();

					if (dev) {
						authorize.requestMatchers(
								"/internal/dev/serasa/auth-check"
						).permitAll();
					}

					authorize.requestMatchers(
							"/v1/credit-analyses"
					).permitAll();

					authorize.anyRequest().denyAll();
				})
				.build();
	}

	static void validateGatewayConfiguration(
			Environment environment) {

		boolean dev = environment.acceptsProfiles(
				Profiles.of("dev")
		);

		boolean hml = environment.acceptsProfiles(
				Profiles.of("hml")
		);

		boolean prd = environment.acceptsProfiles(
				Profiles.of("prd")
		);

		if (dev && (hml || prd)) {
			throw new IllegalStateException(
					"DEV cannot be combined with HML or PRD"
			);
		}

		if (hml && prd) {
			throw new IllegalStateException(
					"HML and PRD cannot be active together"
			);
		}

		if (!dev && !hml && !prd) {
			throw new IllegalStateException(
					"An explicit DEV, HML or PRD profile is required"
			);
		}

		if (hml || prd) {
			boolean gatewayTrusted = environment.getProperty(
					"security.gateway.trust-enabled",
					Boolean.class,
					false
			);

			if (!gatewayTrusted) {
				throw new IllegalStateException(
						"Veste Gateway trust must be explicitly enabled"
				);
			}
		}
	}
}
