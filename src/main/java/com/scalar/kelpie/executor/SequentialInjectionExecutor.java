package com.scalar.kelpie.executor;

import com.scalar.kelpie.modules.Injector;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/** SequentialInjectionExecutor executes each {@link Injector} one by one. */
public class SequentialInjectionExecutor extends InjectionExecutor {

  public SequentialInjectionExecutor(List<Injector> injectors) {
    super(injectors);
  }

  @Override
  public void execute(AtomicBoolean isDone) {
    if (injectors.isEmpty()) {
      return;
    }

    while (!isDone.get()) {
      for (Injector injector : injectors) {
        injector.inject();
        injector.eject();

        if (isDone.get()) {
          break;
        }
      }
    }
  }
}
