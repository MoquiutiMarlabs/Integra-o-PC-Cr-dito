package com.marlabs.pccredito.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CustomerRelationshipTypeTest {

    @Test
    void shouldMapNovoToSerasaNovo() {
        assertEquals(
                "Novo",
                CustomerRelationshipType.NOVO.serasaValue()
        );
    }

    @Test
    void shouldMapReativacaoToSerasaCarteira() {
        assertEquals(
                "Carteira",
                CustomerRelationshipType.REATIVACAO.serasaValue()
        );
    }

    @Test
    void shouldMapAumentoLimiteToSerasaCarteira() {
        assertEquals(
                "Carteira",
                CustomerRelationshipType.AUMENTO_LIMITE.serasaValue()
        );
    }

    @Test
    void shouldAcceptVesteContractValues() {
        assertEquals(
                CustomerRelationshipType.REATIVACAO,
                CustomerRelationshipType.fromValue("REATIVACAO")
        );

        assertEquals(
                CustomerRelationshipType.AUMENTO_LIMITE,
                CustomerRelationshipType.fromValue("AUMENTO_LIMITE")
        );
    }

    @Test
    void shouldRejectInternalSerasaValue() {
        assertThrows(
                IllegalArgumentException.class,
                () -> CustomerRelationshipType.fromValue("Carteira")
        );
    }
}
