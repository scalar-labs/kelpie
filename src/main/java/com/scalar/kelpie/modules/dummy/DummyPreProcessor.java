package com.scalar.kelpie.modules.dummy;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.modules.PreProcessor;

/** DummyPreProcessor is a dummy module of {@link PreProcessor} and executes nothing. */
public class DummyPreProcessor extends PreProcessor {

  public DummyPreProcessor(Config config) {
    super(config);
  }

  @Override
  public void execute() {
    // nothing to do
  }

  @Override
  public void close() {}
}
