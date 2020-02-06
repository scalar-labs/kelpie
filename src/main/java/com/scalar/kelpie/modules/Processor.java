package com.scalar.kelpie.modules;

import com.scalar.kelpie.config.Config;

public abstract class Processor extends Module {

  public Processor(Config config) {
    super(config);
  }

  public abstract void execute();
}
