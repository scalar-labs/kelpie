package com.scalar.kelpie.modules;

import com.scalar.kelpie.config.Config;

public abstract class PostProcessor implements Module {
  protected Config config;

  public PostProcessor(Config config) {
    this.config = config;
  }

  public abstract void execute();
}
