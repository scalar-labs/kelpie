package com.scalar.kelpie.executor;

import com.google.inject.Inject;
import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.exception.InjectionException;
import com.scalar.kelpie.exception.PostProcessException;
import com.scalar.kelpie.modules.Injector;
import com.scalar.kelpie.modules.PostProcessor;
import com.scalar.kelpie.modules.PreProcessor;
import com.scalar.kelpie.modules.Processor;
import com.scalar.kelpie.monitor.PerformanceMonitor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import javax.annotation.concurrent.Immutable;

/** KelpieExecutor controls execution of all modules. */
@Immutable
public class KelpieExecutor {
  private final Config config;
  private final PreProcessor preProcessor;
  private final Processor processor;
  private final PostProcessor postProcessor;
  private final List<Injector> injectors;
  private final AtomicBoolean isDone;

  @Inject
  public KelpieExecutor(
      Config config,
      PreProcessor preProcessor,
      Processor processor,
      PostProcessor postProcessor,
      List<Injector> injectors) {
    this.config = config;
    this.preProcessor = preProcessor;
    this.processor = processor;
    this.postProcessor = postProcessor;
    this.injectors = injectors;

    this.isDone = new AtomicBoolean(false);
  }

  public void execute() {
    if (config.isPerformanceMonitorEnabled()) {
      PerformanceMonitor pm = new PerformanceMonitor(config);
      processor.setPerformanceMonitor(pm);
      postProcessor.setPerformanceMonitor(pm);
    }

    try {
      preProcessor.execute();
      preProcessor.close();

      processor.setPreviousState(preProcessor.getState());
      executeConcurrently();
      processor.close();

      postProcessor.setPreviousState(processor.getState());
      postProcessor.execute();
      postProcessor.close();
    } catch (PostProcessException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Process failure", e);
    }
  }

  private void executeConcurrently() {
    int concurrency = (int) config.getConcurrency();
    ExecutorService es = Executors.newFixedThreadPool(concurrency + 1);
    List<CompletableFuture> futures = new ArrayList<>();

    // Processor
    IntStream.range(0, concurrency)
        .forEach(
            i -> {
              CompletableFuture<Void> future =
                  CompletableFuture.runAsync(
                      () -> {
                        processor.execute();
                      },
                      es);
              futures.add(future);
            });

    // Injectors
    InjectionExecutor injectionExecutor = loadInjectionExecutor();
    CompletableFuture<Void> injectionFuture =
        CompletableFuture.runAsync(
            () -> {
              injectionExecutor.execute(isDone);
            },
            es);

    // Wait for completion of all processor.execute()
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();

    // Stop the injector gracefully
    isDone.set(true);

    // Wait for completion of the injectors
    injectionFuture.join();

    injectionExecutor.close();
  }

  private InjectionExecutor loadInjectionExecutor() {
    String name = config.getInjectionExecutor().get();
    try {
      Class clazz = Class.forName(name);
      Class[] types = {List.class};
      Object[] args = {injectors};

      return (InjectionExecutor) clazz.getConstructor(types).newInstance(args);
    } catch (Exception e) {
      throw new InjectionException("Failed to load InjectionExecutor " + name, e);
    }
  }
}
