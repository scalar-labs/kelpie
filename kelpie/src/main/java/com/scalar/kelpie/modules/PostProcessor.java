package com.scalar.kelpie.modules;

import com.scalar.kelpie.config.Config;

/** PostProcessor executes some tasks after {@link Processor#execute()} finishes. */
public abstract class PostProcessor extends Module {

  public PostProcessor(Config config) {
    super(config);
  }

  public abstract void execute();
}
