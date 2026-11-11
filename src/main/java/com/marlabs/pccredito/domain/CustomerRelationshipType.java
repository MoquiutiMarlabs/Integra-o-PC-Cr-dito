package com.marlabs.pccredito.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CustomerRelationshipType {

    NOVO("Novo"),
    REATIVACAO("Carteira"),
    AUMENTO_LIMITE("Carteira");
    private final String serasaValue;

    CustomerRelationshipType(String serasaValue) {
        this.serasaValue = serasaValue;
    }

    public String serasaValue() {
        return serasaValue;
    }

    @JsonValue
    public String jsonValue() {
        return name();
    }

    @JsonCreator
    public static CustomerRelationshipType fromValue(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        for (CustomerRelationshipType type : values()) {
            if (type.name().equals(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException(
                "tipoConsulta must be NOVO, REATIVACAO or AUMENTO_LIMITE"
        );
    }
}