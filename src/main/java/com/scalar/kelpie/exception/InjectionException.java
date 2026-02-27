package com.scalar.kelpie.exception;

/**
 * Thrown to indicate that an error occurred while injecting an injector
 */
public class InjectionException extends RuntimeException {

  /**
   * Constructs a new {@code InjectionException} with the specified detail message.
   *
   * @param message the detail message describing the cause of the exception
   */
  public InjectionException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@code InjectionException} with the specified detail message.
   *
   * @param message the detail message describing the cause of the exception
   * @param  cause the underlying cause of the exception
   */
  public InjectionException(String message, Throwable cause) {
    super(message, cause);
  }
}
