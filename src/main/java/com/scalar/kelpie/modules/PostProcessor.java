package com.scalar.kelpie.modules;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.stats.Stats;

/** PostProcessor executes a process after {@link Processor#execute()} finishes. */
public abstract class PostProcessor extends Module {
  private Stats stats;

  public PostProcessor(Config config) {
    super(config);
  }

  public abstract void execute();

  /**
   * Returns {@link com.scalar.kelpie.stats.Stats}.
   *
   * @return stats {@link com.scalar.kelpie.stats.Stats}
   */
  public Stats getStats() {
    return stats;
  }

  /**
   * Sets {@link com.scalar.kelpie.stats.Stats}.
   *
   * @param stats {@link com.scalar.kelpie.stats.Stats}
   */
  public void setStats(Stats stats) {
    this.stats = stats;
  }

  /** Outputs the summary of the statistics. */
  protected void getSummary() {
    if (stats == null) {
      return;
    }

    logInfo(stats.getSummary());
  }
}
