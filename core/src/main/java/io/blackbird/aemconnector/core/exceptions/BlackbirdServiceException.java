package io.blackbird.aemconnector.core.exceptions;

public class BlackbirdServiceException extends RuntimeException {
    public BlackbirdServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public BlackbirdServiceException(String message) {
        super(message);
    }
}
