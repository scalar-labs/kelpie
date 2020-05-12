package com.scalar.kelpie.modules;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.monitor.PerformanceMonitor;
import java.util.function.Supplier;

/** TimeConsumingProcessor executes actual tests for the configured time. before exe */
public abstract class TimeConsumingProcessor extends Processor {
  public TimeConsumingProcessor(Config config) {
    super(config);
  }

  /** Runs an {@code operation} repeatedly for {@code run_for_sec} after ramping up. */
  public final void execute() {
    Supplier<Boolean> operation = makeOperation();
    PerformanceMonitor performanceMonitor = getPerformanceMonitor();

    logInfo("Ramping up...");
    long end = System.currentTimeMillis() + config.getRampForSec() * 1000L;
    do {
      operation.get();
    } while (System.currentTimeMillis() < end);

    logInfo("Start measuring");
    end = System.currentTimeMillis() + config.getRunForSec() * 1000L;
    do {
      long start = System.currentTimeMillis();
      if (performanceMonitor != null) {
        if (operation.get()) {
          performanceMonitor.recordLatency(System.currentTimeMillis() - start);
        } else {
          performanceMonitor.recordFailure();
        }
      }
    } while (System.currentTimeMillis() < end);
  }

  /**
   * Returns an {@code operation}. The operation should be {@link Supplier} which returns boolean.
   * If this is true, its latency is recorded.
   *
   * @param operation {@link Supplier} to execute a task
   */
  protected abstract Supplier<Boolean> makeOperation();
}
