package com.scalar.kelpie.executor;

import com.scalar.kelpie.modules.Injector;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class RandomInjectionExecutor extends InjectionExecutor {
  private final Random random;

  public RandomInjectionExecutor(List<Injector> injectors) {
    super(injectors);
    this.random = new Random(System.currentTimeMillis());
  }

  @Override
  public void execute(AtomicBoolean isDone) {
    if (injectors.isEmpty()) {
      return;
    }

    while (!isDone.get()) {
      // Choose an injector randomly
      int index = random.nextInt(injectors.size());
      Injector injector = injectors.get(index);

      injector.inject();

      injector.eject();
    }
  }
}
