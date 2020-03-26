package com.scalar.kelpie.exception;

public class PreProcessException extends RuntimeException {

  public PreProcessException(String message) {
    super(message);
  }

  public PreProcessException(String message, Throwable cause) {
    super(message, cause);
  }
}
