package com.scalar.kelpie.modules.dummy;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.modules.PostProcessor;

public class DummyPostProcessor extends PostProcessor {

  public DummyPostProcessor(Config config) {
    super(config);
  }

  @Override
  public void execute() {
    // nothing to do
  }
}
