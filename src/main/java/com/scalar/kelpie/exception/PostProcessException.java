package com.scalar.kelpie.exception;

public class PostProcessException extends RuntimeException {

  public PostProcessException(String message) {
    super(message);
  }

  public PostProcessException(String message, Throwable cause) {
    super(message, cause);
  }
}
