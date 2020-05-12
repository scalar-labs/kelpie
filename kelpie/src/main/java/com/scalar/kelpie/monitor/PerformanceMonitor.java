package com.scalar.kelpie.monitor;

import com.scalar.kelpie.config.Config;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.HdrHistogram.ConcurrentHistogram;
import org.HdrHistogram.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** PerformanceMonitor records latencies and summarize them. */
public class PerformanceMonitor {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final Config config;
  private final Histogram histogram;
  private final AtomicLong failureCount = new AtomicLong(0L);

  /**
   * Constructs a {@code PerformanceMonitor} with {@link Config}.
   *
   * @param config {@link Config}
   */
  public PerformanceMonitor(Config config) {
    this.config = config;
    this.histogram = new ConcurrentHistogram((int) config.getSignificantDigits());
  }

  /**
   * Records a latency.
   *
   * @param latencyMillis a latency to be recorded
   */
  public void recordLatency(long latencyMillis) {
    histogram.recordValue(latencyMillis);
  }

  /** Records a failure. */
  public void recordFailure() {
    failureCount.incrementAndGet();
  }

  /**
   * Returns a throughput to be calculated with recorded latencies.
   *
   * @param runForSec time in second for which latencies are recorded
   * @return throughput (operation per second)
   */
  public double getThroughput(long runForSec) {
    return round((double) histogram.getTotalCount() / runForSec);
  }

  /**
   * Returns the number of failure.
   *
   * @return failure count
   */
  public long getFailureCount() {
    return failureCount.get();
  }

  /**
   * Returns an average latency.
   *
   * @return average latency in millisecond
   */
  public double getMeanLatency() {
    return round(histogram.getMean());
  }

  /**
   * Returns a standard deviation of latencies.
   *
   * @return standard deviation of latencies in millisecond
   */
  public double getStandardDeviation() {
    return round(histogram.getStdDeviation());
  }

  /**
   * Returns the maximum latency.
   *
   * @return the maximum latency in millisecond
   */
  public long getMaxLatency() {
    return histogram.getMaxValue();
  }

  /**
   * Returns the minimum latency.
   *
   * @return the minimum latency in millisecond
   */
  public long getMinLatency() {
    return histogram.getMinValue();
  }

  /**
   * Returns the latency at the given percentile.
   *
   * @param percentile a percentile of latencies
   * @return a latency at the given percentile in millisecond
   */
  public long getLatencyAtPercentile(double percentile) {
    return histogram.getValueAtPercentile(percentile);
  }

  /**
   * Outputs a summary of the performance.
   *
   * @return a summary of the performance
   */
  public String getSummary() {
    return "==== Performance Summary ====\n"
        + "Throughput: "
        + getThroughput(config.getRunForSec())
        + " ops\n"
        + "Failed operations: "
        + getFailureCount()
        + "\n"
        + "Mean latency: "
        + getMeanLatency()
        + " ms\n"
        + "SD of latency: "
        + getStandardDeviation()
        + " ms\n"
        + "Max latency: "
        + getMaxLatency()
        + " ms\n"
        + "Latency at 50 percentile: "
        + getLatencyAtPercentile(50.0)
        + " ms\n"
        + "Latency at 90 percentile: "
        + getLatencyAtPercentile(90.0)
        + " ms\n"
        + "Latency at 99 percentile: "
        + getLatencyAtPercentile(99.0)
        + " ms\n";
  }

  /**
   * Run a monitor task to show the throughput per second
   *
   * @param isDone if true, other tasks have finished
   */
  public void runMonitor(AtomicBoolean isDone) {
    if (!config.isProgressThroughputEnabled()) {
      return;
    }

    MonitorTask task = new MonitorTask(isDone);
    task.run();
  }

  private double round(double v) {
    return new BigDecimal(v)
        .round(new MathContext((int) config.getSignificantDigits()))
        .doubleValue();
  }

  private class MonitorTask implements Runnable {
    private AtomicBoolean isDone;

    public MonitorTask(AtomicBoolean isDone) {
      this.isDone = isDone;
    }

    @Override
    public void run() {
      long prevCount = 0L;
      long prevTime = System.currentTimeMillis();
      long waitTime = 1000L;
      long prevFailures = 0L;

      while (!isDone.get()) {
        if (waitTime > 0L) {
          try {
            Thread.sleep((int) waitTime);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }

        long currentCount = histogram.getTotalCount();
        long currentTime = System.currentTimeMillis();
        long currentFailures = getFailureCount();

        double throughput = (currentCount - prevCount) * 1000.0 / (currentTime - prevTime);
        long failures = currentFailures - prevFailures;

        logger.info("Throughput: " + round(throughput) + " ops " + "Failed: " + failures);

        // The next waitTime will be 1000 - (currentTime - prevTime - 1000)
        waitTime = 2000L - currentTime + prevTime;
        prevCount = currentCount;
        prevTime = currentTime;
        prevFailures = currentFailures;
      }
    }
  }
}
