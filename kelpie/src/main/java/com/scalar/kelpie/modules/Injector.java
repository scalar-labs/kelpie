package com.scalar.kelpie.modules;

import com.scalar.kelpie.config.Config;

public abstract class Injector extends Module {

  public Injector(Config config) {
    super(config);
  }

  public abstract void inject();

  public abstract void eject();
}
