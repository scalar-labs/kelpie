package com.scalar.kelpie.modules;

import com.scalar.kelpie.config.Config;

public abstract class PreProcessor implements Module {
  protected Config config;

  public PreProcessor(Config config) {
    this.config = config;
  }

  public abstract void execute();
}
