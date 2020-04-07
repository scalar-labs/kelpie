package com.scalar.kelpie.modules;

import com.scalar.kelpie.config.Config;

/** PreProcessor executes some tasks before {@link Processor#execute()}. */
public abstract class PreProcessor extends Module {

  public PreProcessor(Config config) {
    super(config);
  }

  public abstract void execute();
}
