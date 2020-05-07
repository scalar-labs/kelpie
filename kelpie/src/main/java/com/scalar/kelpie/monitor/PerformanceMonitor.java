package com.scalar.kelpie.monitor;

import com.scalar.kelpie.config.Config;
import org.HdrHistogram.ConcurrentHistogram;
import org.HdrHistogram.Histogram;

public class PerformanceMonitor {
  private final Histogram histogram;

  public PerformanceMonitor(Config config) {
    int significantDigits = (int) config.getSignificantDigits();
    this.histogram = new ConcurrentHistogram(significantDigits);
  }

  public void recordLatency(long latencyMillis) {
    histogram.recordValue(latencyMillis);
  }

  public double getThroughput(long runForSec) {
    return (double) histogram.getTotalCount() / runForSec;
  }

  public double getMeanLatency() {
    return histogram.getMean();
  }

  public double getStandardDeviation() {
    return histogram.getStdDeviation();
  }

  public long getMaxLatency() {
    return histogram.getMaxValue();
  }

  public long getMinLatency() {
    return histogram.getMinValue();
  }

  public long getLatencyAtPercentile(double percentile) {
    return histogram.getValueAtPercentile(percentile);
  }
}
