package io.blackbird.aemconnector.core.exceptions;

public class BlackbirdInternalErrorException extends Exception {
    public BlackbirdInternalErrorException(String message) {
        super(message);
    }

    public BlackbirdInternalErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
