package com.scalar.kelpie.modules;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.monitor.PerformanceMonitor;
import java.util.function.Supplier;

/** Processor executes actual tests. */
public abstract class Processor extends Module {
  private PerformanceMonitor monitor;
  private boolean isMonitored = false;

  public Processor(Config config) {
    super(config);
  }

  public abstract void execute();

  public void setPerformanceMonitor(PerformanceMonitor monitor) {
    this.monitor = monitor;
    this.isMonitored = true;
  }

  public void ramp(Supplier<Boolean> op) {
    long end = System.currentTimeMillis() + config.getRampForSec() * 1000L;
    do {
      op.get();
    } while (System.currentTimeMillis() < end);
  }

  public void run(Supplier<Boolean> op) {
    long end = System.currentTimeMillis() + config.getRunForSec() * 1000L;
    do {
      long s = System.currentTimeMillis();
      if (op.get() && isMonitored) {
        monitor.recordLatency(System.currentTimeMillis() - s);
      }
    } while (System.currentTimeMillis() < end);
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
