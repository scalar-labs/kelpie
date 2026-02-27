package com.scalar.kelpie.exception;

/**
 * Thrown to indicate that an error occurred while executing Processor
 */
public class ProcessException extends RuntimeException {

  /**
   * Constructs a new {@code ProcessException} with the specified detail message.
   *
   * @param message the detail message describing the cause of the exception
   */
  public ProcessException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@code ProcessException} with the specified detail message.
   *
   * @param message the detail message describing the cause of the exception
   * @param  cause the underlying cause of the exception
   */
  public ProcessException(String message, Throwable cause) {
    super(message, cause);
  }
}
