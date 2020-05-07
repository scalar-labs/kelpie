package com.scalar.kelpie.modules;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.monitor.PerformanceMonitor;

/** PostProcessor executes some tasks after {@link Processor#execute()} finishes. */
public abstract class PostProcessor extends Module {
  private PerformanceMonitor monitor;

  public PostProcessor(Config config) {
    super(config);
  }

  public abstract void execute();

  public void setPerformanceMonitor(PerformanceMonitor monitor) {
    this.monitor = monitor;
  }

  protected void summary() {
    if (!config.isPerformanceMonitorEnabled()) {
      return;
    }

    String summary =
        "==== Performance Summary ====\n"
            + "Throughput: "
            + monitor.getThroughput(config.getRunForSec())
            + " ops\n"
            + "Mean latency: "
            + monitor.getMeanLatency()
            + " ms\n"
            + "SD of latency: "
            + monitor.getStandardDeviation()
            + " ms\n"
            + "Max latency: "
            + monitor.getMaxLatency()
            + " ms\n"
            + "Latency at 50 percentile: "
            + monitor.getLatencyAtPercentile(50.0)
            + " ms\n"
            + "Latency at 90 percentile: "
            + monitor.getLatencyAtPercentile(90.0)
            + " ms\n"
            + "Latency at 99 percentile: "
            + monitor.getLatencyAtPercentile(99.0)
            + " ms\n";

    logInfo(summary);
  }
}
