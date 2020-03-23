package com.scalar.kelpie.modules;

import com.scalar.kelpie.config.Config;

public abstract class Processor extends Module {

  public Processor(Config config) {
    super(config);
  }

  public abstract void execute();

  @Override
  protected void logTrace(String message) {
    super.logTrace(prependThreadId(message));
  }

  @Override
  protected void logDebug(String message) {
    super.logDebug(prependThreadId(message));
  }

  @Override
  protected void logInfo(String message) {
    super.logInfo(prependThreadId(message));
  }

  @Override
  protected void logWarn(String message) {
    super.logWarn(prependThreadId(message));
  }

  @Override
  protected void logError(String message) {
    super.logError(prependThreadId(message));
  }

  private String prependThreadId(String message) {
    return "[Thread " + Thread.currentThread().getId() + "] " + message;
  }
}
