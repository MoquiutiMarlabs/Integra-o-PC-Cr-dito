package com.marlabs.pccredito.integration.serasa;

public class InvalidSerasaResponseException
        extends RuntimeException {

    public InvalidSerasaResponseException(String message) {
        super(message);
    }

    public InvalidSerasaResponseException(
            String message,
            Throwable cause) {
        super(message, cause);
    }
}
