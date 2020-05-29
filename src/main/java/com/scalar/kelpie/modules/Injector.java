package com.scalar.kelpie.modules;

import com.scalar.kelpie.config.Config;

/**
 * An Injector abstraction to execute some arbitrary tasks while {@link Processor} executes a test.
 */
public abstract class Injector extends Module {

  /**
   * Constructs a {@code Injector} with {@link Config}.
   *
   * @param config {@link Config}
   */
  public Injector(Config config) {
    super(config);
  }

  /**
   * Injects a task. The task should finish gracefully when {@link #eject()} is invoked.
   */
  public abstract void inject();

  /**
   * Ejects a task that is injected with {@link #inject()}.
   */
  public abstract void eject();
}
