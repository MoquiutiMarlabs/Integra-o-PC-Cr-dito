package com.marlabs.pccredito.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CustomerRelationshipType {

    NOVO("Novo"),
    CARTEIRA("Carteira");

    private final String serasaValue;

    CustomerRelationshipType(String serasaValue) {
        this.serasaValue = serasaValue;
    }

    public String serasaValue() {
        return serasaValue;
    }

    @JsonValue
    public String jsonValue() {
        return serasaValue;
    }

    @JsonCreator
    public static CustomerRelationshipType fromValue(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        for (CustomerRelationshipType type : values()) {
            if (type.serasaValue.equalsIgnoreCase(value)
                    || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException(
                "subproduto2 must be Novo or Carteira"
        );
    }
}