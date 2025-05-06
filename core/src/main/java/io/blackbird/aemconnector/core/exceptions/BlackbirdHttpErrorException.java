package io.blackbird.aemconnector.core.exceptions;

import lombok.Getter;

import javax.servlet.http.HttpServletResponse;

@Getter
public class BlackbirdHttpErrorException extends Exception {
    private final int status;
    private final String error;

    public BlackbirdHttpErrorException(int status, String error, String message) {
        super(message);
        this.status = status;
        this.error = error;
    }

    public static BlackbirdHttpErrorException unauthorized(String message) {
        return new BlackbirdHttpErrorException(
                HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", message);
    }

    public static BlackbirdHttpErrorException conflict(String message) {
        return new BlackbirdHttpErrorException(
                HttpServletResponse.SC_CONFLICT, "Conflict", message);
    }

    public static BlackbirdHttpErrorException badRequest(String message) {
        return new BlackbirdHttpErrorException(
                HttpServletResponse.SC_BAD_REQUEST, "Bad Request", message);
    }

    public static BlackbirdHttpErrorException notFound(String message) {
        return new BlackbirdHttpErrorException(
                HttpServletResponse.SC_NOT_FOUND, "Not Found", message);
    }

    public static BlackbirdHttpErrorException internalServerError(String message) {
        return new BlackbirdHttpErrorException(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error", message);
    }
}
