package io.blackbird.aemconnector.core.exceptions;

public class CopyMergeDitaValidationException extends Exception {

    public CopyMergeDitaValidationException(String message) {
        super(message);
    }

    public CopyMergeDitaValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
