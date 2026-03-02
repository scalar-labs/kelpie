package com.scalar.kelpie.exception;

/**
 * Thrown to indicate that an error occurred while processing a config.
 */
public class IllegalConfigException extends RuntimeException {

  /**
   * Constructs a new {@code IllegalConfigException} with the specified detail message.
   *
   * @param message the detail message describing the cause of the exception
   */
  public IllegalConfigException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@code IllegalConfigException} with the specified detail message.
   *
   * @param message the detail message describing the cause of the exception
   * @param cause the underlying cause of the exception
   */
  public IllegalConfigException(String message, Throwable cause) {
    super(message, cause);
  }
}
