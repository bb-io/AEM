package io.blackbird.aemconnector.core.exceptions;

import lombok.Getter;

@Getter
public class BlackbirdHttpErrorException extends Exception {
    private final int status;
    private final String error;

    public BlackbirdHttpErrorException(int status, String error, String message) {
        super(message);
        this.status = status;
        this.error = error;
    }
}
