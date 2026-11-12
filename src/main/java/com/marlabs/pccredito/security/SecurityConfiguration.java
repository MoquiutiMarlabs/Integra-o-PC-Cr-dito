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

		boolean dev = environment.acceptsProfiles(
				Profiles.of("dev")
		);

		boolean authenticationRequired =
				validateAuthenticationConfiguration(environment);

		IpWhitelistFilter ipWhitelistFilter =
				new IpWhitelistFilter(environment);

		return http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session ->
						session.sessionCreationPolicy(
								SessionCreationPolicy.STATELESS
						)
				)
				.httpBasic(httpBasic -> httpBasic.disable())
				.formLogin(formLogin -> formLogin.disable())
				.logout(logout -> logout.disable())
				.addFilterBefore(
						ipWhitelistFilter,
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

					if (authenticationRequired) {
						// TODO VESTE-SSO:
						// Configurar validacao JWT ou fronteira
						// confiavel com o Gateway, apos definicao
						// formal da arquitetura de autenticacao.
						authorize.anyRequest().denyAll();
					} else {
						authorize.requestMatchers(
								"/v1/credit-analyses"
						).permitAll();

						authorize.anyRequest().denyAll();
					}
				})
				.build();
	}

	static boolean validateAuthenticationConfiguration(
			Environment environment) {

		boolean authenticationRequired =
				environment.getProperty(
						"security.m2m.authentication-required",
						Boolean.class,
						true
				);

		boolean dev = environment.acceptsProfiles(
				Profiles.of("dev")
		);

		boolean hml = environment.acceptsProfiles(
				Profiles.of("hml")
		);

		boolean prd = environment.acceptsProfiles(
				Profiles.of("prd")
		);

		if (!authenticationRequired && (!dev || hml || prd)) {
			throw new IllegalStateException(
					"Authentication cannot be disabled " +
							"outside the exclusive DEV profile"
			);
		}

		return authenticationRequired;
	}
}
