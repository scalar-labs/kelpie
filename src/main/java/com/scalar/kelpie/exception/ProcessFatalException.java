package com.scalar.kelpie.exception;

public class ProcessFatalException extends ProcessException {

  public ProcessFatalException(String message) {
    super(message);
  }

  public ProcessFatalException(String message, Throwable cause) {
    super(message, cause);
  }
}
