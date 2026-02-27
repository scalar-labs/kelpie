package com.scalar.kelpie.exception;

/**
 * Thrown to indicate that an error occurred while executing process
 */
public class ProcessFatalException extends ProcessException {

  /**
   * Constructs a new {@code ProcessFatalException} with the specified detail message.
   *
   * @param message the detail message describing the cause of the exception
   */
  public ProcessFatalException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@code ProcessFatalException} with the specified detail message.
   *
   * @param message the detail message describing the cause of the exception
   * @param  cause the underlying cause of the exception
   */
  public ProcessFatalException(String message, Throwable cause) {
    super(message, cause);
  }
}
