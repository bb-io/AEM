package io.blackbird.aemconnector.core.exceptions;

public class ContentReferenceException extends RuntimeException {
    public ContentReferenceException(String message) {
        super(message);
    }

  public ContentReferenceException(String message, Throwable cause) {
    super(message, cause);
  }
}
