package com.scalar.kelpie.monitor;

import com.scalar.kelpie.config.Config;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.concurrent.atomic.AtomicBoolean;
import org.HdrHistogram.ConcurrentHistogram;
import org.HdrHistogram.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** PerformanceMonitor records latencies and summarize them. */
public class PerformanceMonitor {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final Config config;
  private final Histogram histogram;

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

  /**
   * Returns a throughput to be calculated with recorded latencies.
   *
   * @param runForSec time in second for which latencies are recorded
   */
  public double getThroughput(long runForSec) {
    return round((double) histogram.getTotalCount() / runForSec);
  }

  /** Returns an average latency. */
  public double getMeanLatency() {
    return round(histogram.getMean());
  }

  /** Returns a standard deviation of latencies. */
  public double getStandardDeviation() {
    return round(histogram.getStdDeviation());
  }

  /** Returns the maximum latency. */
  public long getMaxLatency() {
    return histogram.getMaxValue();
  }

  /** Returns the minimuim latency. */
  public long getMinLatency() {
    return histogram.getMinValue();
  }

  /**
   * Returns the latency at the given percentile.
   *
   * @param percentile a percentile of latencies
   */
  public long getLatencyAtPercentile(double percentile) {
    return histogram.getValueAtPercentile(percentile);
  }

  public void monitor(AtomicBoolean isDone) {
    MonitorTask task = new MonitorTask(isDone);

    task.run();
  }

  /** Outputs the summary of the performance. */
  public String getSummary() {
    return "==== Performance Summary ====\n"
        + "Throughput: "
        + getThroughput(config.getRunForSec())
        + " ops\n"
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

  private double round(double v) {
    return new BigDecimal(v)
        .round(new MathContext((int) config.getSignificantDigits()))
        .doubleValue();
  }

  private class MonitorTask {
    private AtomicBoolean isDone;

    public MonitorTask(AtomicBoolean isDone) {
      this.isDone = isDone;
    }

    public void run() {
      long prevCount = 0L;
      long prevTime = System.currentTimeMillis();
      long waitTime = 1000L;

      while (!isDone.get()) {
        try {
          Thread.sleep((int) waitTime);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }

        long currentCount = histogram.getTotalCount();
        long currentTime = System.currentTimeMillis();

        double throughput = (double) (currentCount - prevCount) / (currentTime - prevTime);

        logger.info("Throughput: " + round(throughput) + " ops");

        prevCount = currentCount;
        waitTime = 2000L - currentTime + prevTime;
        prevTime = currentTime;
      }
    }
  }
}
