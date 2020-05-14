package com.scalar.kelpie.modules;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.stats.Stats;
import java.util.function.Supplier;

/** TimeBasedProcessor executes actual tests for the configured time. */
public abstract class TimeBasedProcessor extends Processor {
  public TimeBasedProcessor(Config config) {
    super(config);
  }

  /** Runs an {@code operation} repeatedly for {@code run_for_sec} after ramping up. */
  public final void execute() {
    Stats stats = getStats();

    Supplier<Boolean> operation =
        () -> {
          try {
            executeEach();
            return true;
          } catch (Exception e) {
            return false;
          }
        };

    long end = System.currentTimeMillis() + config.getRampForSec() * 1000L;
    do {
      operation.get();
    } while (System.currentTimeMillis() < end);

    end = System.currentTimeMillis() + config.getRunForSec() * 1000L;
    do {
      long start = System.currentTimeMillis();
      if (stats != null) {
        if (operation.get()) {
          stats.recordLatency(System.currentTimeMillis() - start);
        } else {
          stats.recordFailure();
        }
      }
    } while (System.currentTimeMillis() < end);
  }

  /**
   * Execute an operation. This method is invoked repeatedly in {@link execute()} for {@code
   * run_for_sec}. If a failure which you don't want to record its latency happens, this method
   * should throw an exception. The exception will be caught in {@link execute()}.
   */
  protected abstract void executeEach();
}
