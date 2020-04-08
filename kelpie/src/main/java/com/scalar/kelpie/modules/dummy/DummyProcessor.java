package com.scalar.kelpie.modules.dummy;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.modules.Processor;

/** DummyProcessor is a dummy module of {@link Processor} and executes nothing. */
public class DummyProcessor extends Processor {

  public DummyProcessor(Config config) {
    super(config);
  }

  @Override
  public void execute() {
    // nothing to do
  }

  @Override
  public void close() {}
}
