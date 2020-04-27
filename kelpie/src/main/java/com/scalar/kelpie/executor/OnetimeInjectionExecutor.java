package com.scalar.kelpie.executor;

import com.scalar.kelpie.modules.Injector;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/** OnetimeInjectionExecutor executes all {@link Injector}s once. */
public class OnetimeInjectionExecutor extends InjectionExecutor {

  public OnetimeInjectionExecutor(List<Injector> injectors) {
    super(injectors);
  }

  @Override
  public void execute(AtomicBoolean isDone) {
    if (injectors.isEmpty()) {
      return;
    }

    ExecutorService es = Executors.newFixedThreadPool(injectors.size());
    List<CompletableFuture> futures = new ArrayList<>();

    injectors.forEach(
        i -> {
          CompletableFuture<Void> future =
              CompletableFuture.runAsync(
                  () -> {
                    executeInjection(i);
                  },
                  es);
          futures.add(future);
        });

    while (!isDone.get()) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // ignored
      }
    }
    es.shutdownNow();

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();
  }

  private synchronized void executeInjection(Injector injector) {
    injector.inject();

    try {
      wait();
    } catch (InterruptedException e) {
      // ignored
    }

    injector.eject();
  }
}