package com.scalar.kelpie.exception;

/**
 * Thrown to indicate that an error occurred while executing PostProcessor
 */
public class PostProcessException extends RuntimeException {

  /**
   * Constructs a new {@code PostProcessException} with the specified detail message.
   *
   * @param message the detail message describing the cause of the exception
   */
  public PostProcessException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@code PostProcessException} with the specified detail message.
   *
   * @param message the detail message describing the cause of the exception
   * @param  cause the underlying cause of the exception
   */
  public PostProcessException(String message, Throwable cause) {
    super(message, cause);
  }
}
