package com.scalar.kelpie.modules;

import com.scalar.kelpie.config.Config;

public abstract class Injector implements Module {
  protected Config config;

  public Injector(Config config) {
    this.config = config;
  }

  public abstract void inject();

  public abstract void eject();
}
