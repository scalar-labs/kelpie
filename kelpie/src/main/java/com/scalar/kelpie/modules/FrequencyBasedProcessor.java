package com.scalar.kelpie.modules;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.monitor.PerformanceMonitor;
import java.util.function.Supplier;
import java.util.stream.LongStream;

/** FrequencyBasedProcessor executes actual tests by the configured count. */
public abstract class FrequencyBasedProcessor extends Processor {
  public FrequencyBasedProcessor(Config config) {
    super(config);
  }

  /** Runs an {@code operation} repeatedly by {@code num_operations} after ramping up. */
  public final void execute() {
    PerformanceMonitor performanceMonitor = getPerformanceMonitor();

    Supplier<Boolean> operation =
        () -> {
          try {
            executeEach();
            return true;
          } catch (Exception e) {
            return false;
          }
        };

    long num = config.getNumOperationsForRampUp() / config.getConcurrency();
    LongStream.range(0, num).forEach(i -> operation.get());

    num = config.getNumOperations() / config.getConcurrency();
    LongStream.range(0, num)
        .forEach(
            i -> {
              long start = System.currentTimeMillis();
              if (performanceMonitor != null) {
                if (operation.get()) {
                  performanceMonitor.recordLatency(System.currentTimeMillis() - start);
                } else {
                  performanceMonitor.recordFailure();
                }
              }
            });
  }

  /**
   * Execute some operations. This method is invoked repeatedly in {@link execute()} by {@code
   * num_operations}. If a failure which you don't want to record its latency happens, this method
   * should throw an exception. The exception will be caught in {@link execute()}.
   */
  protected abstract void executeEach();
}
