package com.scalar.kelpie.modules;

import com.scalar.kelpie.config.Config;

public abstract class PostProcessor extends Module {

  public PostProcessor(Config config) {
    super(config);
  }

  public abstract void execute();
}
