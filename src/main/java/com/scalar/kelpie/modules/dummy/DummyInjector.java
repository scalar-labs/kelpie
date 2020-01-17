package com.scalar.kelpie.modules.dummy;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.modules.Injector;

public class DummyInjector extends Injector {
  protected Config config;

  public void initialize(Config config) {
    this.config = config;
  }

  @Override
  public void inject() {
    // nothing to do
  }

  @Override
  public void eject() {
    // nothing to do
  }
}
