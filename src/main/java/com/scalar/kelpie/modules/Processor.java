package com.scalar.kelpie.modules;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.stats.Stats;

/** Processor executes operations. */
public abstract class Processor extends Module {
  private Stats stats;

  public Processor(Config config) {
    super(config);
  }

  public abstract void execute();

  /**
   * Returns {@link com.scalar.kelpie.stats.Stats}.
   *
   * @return stats {@link com.scalar.kelpie.stats.Stats}
   */
  public Stats getStats() {
    return stats;
  }

  /**
   * Sets {@link com.scalar.kelpie.stats.Stats}.
   *
   * @param stats {@link com.scalar.kelpie.stats.Stats}
   */
  public void setStats(Stats stats) {
    this.stats = stats;
  }

  @Override
  protected void logTrace(String message) {
    super.logTrace(prependThreadId(message));
  }

  @Override
  protected void logTrace(String message, Throwable e) {
    super.logTrace(prependThreadId(message), e);
  }

  @Override
  protected void logDebug(String message) {
    super.logDebug(prependThreadId(message));
  }

  @Override
  protected void logDebug(String message, Throwable e) {
    super.logDebug(prependThreadId(message), e);
  }

  @Override
  protected void logInfo(String message) {
    super.logInfo(prependThreadId(message));
  }

  @Override
  protected void logInfo(String message, Throwable e) {
    super.logInfo(prependThreadId(message), e);
  }

  @Override
  protected void logWarn(String message) {
    super.logWarn(prependThreadId(message));
  }

  @Override
  protected void logWarn(String message, Throwable e) {
    super.logWarn(prependThreadId(message), e);
  }

  @Override
  protected void logError(String message) {
    super.logError(prependThreadId(message));
  }

  @Override
  protected void logError(String message, Throwable e) {
    super.logError(prependThreadId(message), e);
  }

  private String prependThreadId(String message) {
    return "[Thread " + Thread.currentThread().getId() + "] " + message;
  }
}
