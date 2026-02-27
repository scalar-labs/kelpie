package com.scalar.kelpie.exception;

/**
 * Thrown to indicate that an error occurred while loading a module.
 */
public class ModuleLoadException extends RuntimeException {

  /**
   * Constructs a new {@code ModuleLoadException} with the specified detail message.
   *
   * @param message the detail message describing the cause of the exception
   */
  public ModuleLoadException(String message) {
    super(message);
  }

  /**
   * Constructs a new {@code ModuleLoadException} with the specified detail message.
   *
   * @param message the detail message describing the cause of the exception
   * @param  cause the underlying cause of the exception
   */
  public ModuleLoadException(String message, Throwable cause) {
    super(message, cause);
  }
}
