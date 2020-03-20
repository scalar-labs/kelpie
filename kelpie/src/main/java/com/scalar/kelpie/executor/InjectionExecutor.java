package com.scalar.kelpie.executor;

import com.scalar.kelpie.modules.Injector;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class InjectionExecutor {
  protected List<Injector> injectors;

  public InjectionExecutor(List<Injector> injectors) {
    this.injectors = injectors;
  }

  public abstract void execute(AtomicBoolean isDone);

  public void close() {
    for (Injector injector : injectors) {
      try {
        injector.close();
      } catch (Exception e) {
        System.err.println(e.getMessage());
        throw new RuntimeException("An injector failed to close");
      }
    }
  }
}
