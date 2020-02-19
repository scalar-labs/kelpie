package com.scalar.kelpie.modules;

import com.scalar.kelpie.config.Config;

public abstract class Module {
  protected Config config;

  public Module(Config config) {
    this.config = config;
  }
}
