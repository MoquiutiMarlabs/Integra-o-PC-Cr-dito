package com.marlabs.pccredito.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class SecurityConfigurationTest {

    @Test
    void shouldAllowDisabledAuthenticationInDev() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty(
                        "security.m2m.authentication-required",
                        "false"
                );

        environment.setActiveProfiles("dev");

        assertFalse(
                SecurityConfiguration
                        .validateAuthenticationConfiguration(environment)
        );
    }

    @Test
    void shouldRejectDisabledAuthenticationInHml() {
        assertAuthenticationCannotBeDisabled("hml");
    }

    @Test
    void shouldRejectDisabledAuthenticationInPrd() {
        assertAuthenticationCannotBeDisabled("prd");
    }

    @Test
    void shouldRejectDisabledAuthenticationWithoutProfile() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty(
                        "security.m2m.authentication-required",
                        "false"
                );

        assertThrows(
                IllegalStateException.class,
                () -> SecurityConfiguration
                        .validateAuthenticationConfiguration(environment)
        );
    }

    @Test
    void shouldRejectDevCombinedWithHml() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty(
                        "security.m2m.authentication-required",
                        "false"
                );

        environment.setActiveProfiles("dev", "hml");

        assertThrows(
                IllegalStateException.class,
                () -> SecurityConfiguration
                        .validateAuthenticationConfiguration(environment)
        );
    }

    @Test
    void shouldRequireAuthenticationByDefault() {
        MockEnvironment environment = new MockEnvironment();

        environment.setActiveProfiles("hml");

        assertTrue(
                SecurityConfiguration
                        .validateAuthenticationConfiguration(environment)
        );
    }

    private void assertAuthenticationCannotBeDisabled(
            String profile) {

        MockEnvironment environment = new MockEnvironment()
                .withProperty(
                        "security.m2m.authentication-required",
                        "false"
                );

        environment.setActiveProfiles(profile);

        assertThrows(
                IllegalStateException.class,
                () -> SecurityConfiguration
                        .validateAuthenticationConfiguration(environment)
        );
    }
}
