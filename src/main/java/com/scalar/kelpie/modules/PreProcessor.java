package com.scalar.kelpie.modules;

import com.scalar.kelpie.config.Config;

/** PreProcessor executes a process before {@link Processor#execute()}. */
public abstract class PreProcessor extends Module {

  /**
   * Creates a PreProcessor.
   *
   * @param config configuration object
   */
  public PreProcessor(Config config) {
    super(config);
  }

  /**
   * Executes PreProcessor
   */
  public abstract void execute();
}
