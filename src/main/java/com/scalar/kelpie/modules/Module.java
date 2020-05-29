package com.scalar.kelpie.modules;

import com.scalar.kelpie.config.Config;
import javax.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Module abstraction to execute tasks. */
public abstract class Module implements AutoCloseable {
  protected static final JsonObject DEFAULT_STATE = JsonObject.EMPTY_JSON_OBJECT;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  protected Config config;
  private JsonObject state;
  private JsonObject previousState;

  /**
   * Constructs a {@code Module} with {@link Config}.
   *
   * @param config {@link Config}
   */
  public Module(Config config) {
    this.config = config;
    this.state = DEFAULT_STATE;
  }

  /**
   * Returns the state of this module. This state is given to the next module for the communication
   * between modules.
   *
   * @return {@code JsonObject} as the state
   */
  public final JsonObject getState() {
    return this.state;
  }

  /**
   * Returns the state of the previous module which is set before this module executes tasks by
   * {@link com.scalar.kelpie.executor.KelpieExecutor}. For example, the previous state of {@link
   * Processor} means the state of {@link PreProcessor}.
   *
   * @return {@code JsonObject} as the previous state
   */
  public final JsonObject getPreviousState() {
    return this.previousState;
  }

  /**
   * Sets the state which is passed to the next module after this module's execution is completed.
   *
   * @param state the state of this module as a {@code JsonObject}
   */
  public final void setState(JsonObject state) {
    this.state = state;
  }

  /**
   * Sets the previous state. Only {@link com.scalar.kelpie.executor.KelpieExecutor} invokes this
   * method to update the state after each module execution finishes.
   *
   * @param previousState the state of this module as a {@code JsonObject}
   */
  public final void setPreviousState(JsonObject previousState) {
    this.previousState = previousState;
  }

  /**
   * Outputs a log for logging level {@code TRACE}.
   *
   * @param message a log message
   */
  protected void logTrace(String message) {
    logger.trace(message);
  }

  /**
   * Outputs a log for logging level {@code TRACE}.
   *
   * @param message a log message
   * @param e an exception which causes the log
   */
  protected void logTrace(String message, Throwable e) {
    logger.trace(message, e);
  }

  /**
   * Outputs a log for logging level {@code DEBUG}.
   *
   * @param message a log message
   */
  protected void logDebug(String message) {
    logger.debug(message);
  }

  /**
   * Outputs a log for logging level {@code DEBUG}.
   *
   * @param message a log message
   * @param e an exception which causes the log
   */
  protected void logDebug(String message, Throwable e) {
    logger.debug(message, e);
  }

  /**
   * Outputs a log for logging level {@code INFO}.
   *
   * @param message a log message
   */
  protected void logInfo(String message) {
    logger.info(message);
  }

  /**
   * Outputs a log for logging level {@code INFO}.
   *
   * @param message a log message
   * @param e an exception which causes the log
   */
  protected void logInfo(String message, Throwable e) {
    logger.info(message, e);
  }

  /**
   * Outputs a log for logging level {@code WARN}.
   *
   * @param message a log message
   */
  protected void logWarn(String message) {
    logger.warn(message);
  }

  /**
   * Outputs a log for logging level {@code WARN}.
   *
   * @param message a log message
   * @param e an exception which causes the log
   */
  protected void logWarn(String message, Throwable e) {
    logger.warn(message, e);
  }

  /**
   * Outputs a log for logging level {@code ERROR}.
   *
   * @param message a log message
   */
  protected void logError(String message) {
    logger.error(message);
  }

  /**
   * Outputs a log for logging level {@code ERROR}.
   *
   * @param message a log message
   * @param e an exception which causes the log
   */
  protected void logError(String message, Throwable e) {
    logger.error(message, e);
  }
}
