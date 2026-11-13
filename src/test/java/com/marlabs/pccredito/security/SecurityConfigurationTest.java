package com.marlabs.pccredito.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class SecurityConfigurationTest {

    @Test
    void shouldAllowDevWithoutGatewayTrust() {
        var environment = new MockEnvironment();
        environment.setActiveProfiles("dev");

        assertDoesNotThrow(() ->
                SecurityConfiguration.validateGatewayConfiguration(
                        environment
                )
        );
    }

    @Test
    void shouldRejectHmlWithoutGatewayTrust() {
        assertGatewayRequired("hml");
    }

    @Test
    void shouldRejectPrdWithoutGatewayTrust() {
        assertGatewayRequired("prd");
    }

    @Test
    void shouldAllowHmlWithGatewayTrust() {
        assertGatewayAccepted("hml");
    }

    @Test
    void shouldAllowPrdWithGatewayTrust() {
        assertGatewayAccepted("prd");
    }

    @Test
    void shouldRejectDevAndHmlTogether() {
        var environment = new MockEnvironment();
        environment.setActiveProfiles("dev", "hml");

        assertThrows(
                IllegalStateException.class,
                () -> SecurityConfiguration
                        .validateGatewayConfiguration(environment)
        );
    }

    @Test
    void shouldRejectMissingProfile() {
        var environment = new MockEnvironment();

        assertThrows(
                IllegalStateException.class,
                () -> SecurityConfiguration
                        .validateGatewayConfiguration(environment)
        );
    }

    private void assertGatewayRequired(String profile) {
        var environment = new MockEnvironment();
        environment.setActiveProfiles(profile);

        assertThrows(
                IllegalStateException.class,
                () -> SecurityConfiguration
                        .validateGatewayConfiguration(environment)
        );
    }

    private void assertGatewayAccepted(String profile) {
        var environment = new MockEnvironment()
                .withProperty(
                        "security.gateway.trust-enabled",
                        "true"
                );

        environment.setActiveProfiles(profile);

        assertDoesNotThrow(() ->
                SecurityConfiguration.validateGatewayConfiguration(
                        environment
                )
        );
    }
}
