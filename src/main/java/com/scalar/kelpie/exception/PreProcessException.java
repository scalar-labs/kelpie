package com.scalar.kelpie.exception;

/**
 * Thrown to indicate that an error occurred while executing PreProcessor
 */
public class PreProcessException extends RuntimeException {

  /**
   * Constructs a new {@code PreProcessException} with the specified detail message.
   *
   * @param message the detail message describing the cause of the exception
   */
  public PreProcessException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@code PreProcessException} with the specified detail message.
   *
   * @param message the detail message describing the cause of the exception
   * @param  cause the underlying cause of the exception
   */
  public PreProcessException(String message, Throwable cause) {
    super(message, cause);
  }
}
