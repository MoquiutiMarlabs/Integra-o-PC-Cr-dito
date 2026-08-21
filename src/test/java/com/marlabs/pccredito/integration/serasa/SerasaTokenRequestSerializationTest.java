package com.marlabs.pccredito.integration.serasa;

import com.marlabs.pccredito.integration.serasa.dto.SerasaTokenRequest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class SerasaTokenRequestSerializationTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void shouldSerializeTokenRequestUsingSerasaFieldNames() {

        SerasaTokenRequest request = new SerasaTokenRequest(
                "user",
                "password",
                "client-id",
                "client-secret"
        );

        String json = jsonMapper.writeValueAsString(request);

        assertThat(json).contains(
                "\"username\":\"user\"",
                "\"password\":\"password\"",
                "\"client_id\":\"client-id\"",
                "\"client_secret\":\"client-secret\""
        );

        assertThat(json).doesNotContain(
                "\"clientId\"",
                "\"clientSecret\""
        );
    }
}