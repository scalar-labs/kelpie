package com.scalar.kelpie.modules.dummy;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.modules.PostProcessor;

/** DummyPostProcessor is a dummy module of {@link PostProcessor} and executes nothing. */
public class DummyPostProcessor extends PostProcessor {

  public DummyPostProcessor(Config config) {
    super(config);
  }

  @Override
  public void execute() {
    // nothing to do
  }

  @Override
  public void close() {}
}
