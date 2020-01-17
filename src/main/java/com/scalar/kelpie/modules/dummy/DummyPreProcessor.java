package com.scalar.kelpie.modules.dummy;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.modules.PreProcessor;

public class DummyPreProcessor extends PreProcessor {
  protected Config config;

  public void initialize(Config config) {
    this.config = config;
  }

  @Override
  public void execute() {
    // nothing to do
  }
}
