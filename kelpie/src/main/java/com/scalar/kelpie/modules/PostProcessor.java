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

  /**
   * Returns {@link com.scalar.kelpie.monitor.PerformanceMonitor}.
   *
   * @return monitor {@link com.scalar.kelpie.monitor.PerformanceMonitor}
   */
  public PerformanceMonitor getPerformanceMonitor() {
    return monitor;
  }

  /**
   * Sets {@link com.scalar.kelpie.monitor.PerformanceMonitor}.
   *
   * @param monitor {@link com.scalar.kelpie.monitor.PerformanceMonitor}
   */
  public void setPerformanceMonitor(PerformanceMonitor monitor) {
    this.monitor = monitor;
  }

  /** Outputs the summary of the performance. */
  protected void getSummary() {
    if (!config.isPerformanceMonitorEnabled()) {
      return;
    }

    logInfo(monitor.getSummary());
  }
}
