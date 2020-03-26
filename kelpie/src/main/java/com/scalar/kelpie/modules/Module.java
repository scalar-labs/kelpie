package com.scalar.kelpie.modules;

import com.scalar.kelpie.config.Config;
import javax.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Module implements AutoCloseable {
  protected static final JsonObject DEFAULT_STATE = JsonObject.EMPTY_JSON_OBJECT;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  protected Config config;
  private JsonObject state;
  private JsonObject previousState;

  public Module(Config config) {
    this.config = config;
    this.state = DEFAULT_STATE;
  }

  public final JsonObject getState() {
    return this.state;
  }

  public final JsonObject getPreviousState() {
    return this.previousState;
  }

  public final void setState(JsonObject state) {
    this.state = state;
  }

  public final void setPreviousState(JsonObject previousState) {
    this.previousState = previousState;
  }

  protected void logTrace(String message) {
    logger.trace(message);
  }

  protected void logTrace(String message, Throwable e) {
    logger.trace(message, e);
  }

  protected void logDebug(String message) {
    logger.debug(message);
  }

  protected void logDebug(String message, Throwable e) {
    logger.debug(message, e);
  }

  protected void logInfo(String message) {
    logger.info(message);
  }

  protected void logInfo(String message, Throwable e) {
    logger.info(message, e);
  }

  protected void logWarn(String message) {
    logger.warn(message);
  }

  protected void logWarn(String message, Throwable e) {
    logger.warn(message, e);
  }

  protected void logError(String message) {
    logger.error(message);
  }

  protected void logError(String message, Throwable e) {
    logger.error(message, e);
  }
}
