package com.scalar.kelpie.executor;

import com.scalar.kelpie.exception.InjectionException;
import com.scalar.kelpie.modules.Injector;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A InjectionExecutor abstraction for switching injection execution policies. When multiple {@link
 * Injector}s are specified, the way they run concurrently is controlled by {@link
 * InjectionExecutor}.
 */
public abstract class InjectionExecutor {
  protected List<Injector> injectors;

  public InjectionExecutor(List<Injector> injectors) {
    this.injectors = injectors;
  }

  /**
   * Executes {@link Injector}s.
   *
   * @param isDone the execution should finish when this has been set to true
   */
  public abstract void execute(AtomicBoolean isDone);

  /** Close all {@link Injector}s. */
  public void close() {
    for (Injector injector : injectors) {
      try {
        injector.close();
      } catch (Exception e) {
        throw new InjectionException("An injector failed to close", e);
      }
    }
  }
}
