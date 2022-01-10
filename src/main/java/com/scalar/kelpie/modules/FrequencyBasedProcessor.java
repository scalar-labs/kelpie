package com.scalar.kelpie.modules;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.exception.ProcessFatalException;
import com.scalar.kelpie.stats.Stats;
import java.util.function.Supplier;
import java.util.stream.LongStream;

/** FrequencyBasedProcessor executes operations by the configured count. */
public abstract class FrequencyBasedProcessor extends Processor {
  public FrequencyBasedProcessor(Config config) {
    super(config);
  }

  /** Runs an {@code operation} repeatedly by {@code num_operations} after ramping up. */
  public final void execute() {
    Stats stats = getStats();

    Supplier<Boolean> operation =
        () -> {
          try {
            executeEach();
            return true;
          } catch (ProcessFatalException e) {
            throw e;
          } catch (Exception e) {
            if (config.isLogEnabledWhenError()) {
              logError("An error occurred during executing the processor.", e);
            }
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
              if (stats != null) {
                if (operation.get()) {
                  stats.recordLatency(System.currentTimeMillis() - start);
                } else {
                  stats.recordFailure();
                }
              }
            });
  }

  /**
   * Execute an operation. This method is invoked repeatedly in {@link #execute()} by {@code
   * num_operations}. If a failure which you don't want to record its latency happens, this method
   * should throw an exception. The exception will be caught in {@link #execute()}.
   *
   * @throws Exception This exception will be caught in {@link #execute()}
   */
  protected abstract void executeEach() throws Exception;
}
