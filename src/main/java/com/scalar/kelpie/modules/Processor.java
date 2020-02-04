package com.scalar.kelpie.modules;

import com.scalar.kelpie.config.Config;

public abstract class Processor implements Module {
  protected Config config;

  public Processor(Config config) {
    this.config = config;
  }

  public abstract void execute();
}
