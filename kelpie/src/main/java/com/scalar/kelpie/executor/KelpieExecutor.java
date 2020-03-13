package com.scalar.kelpie.executor;

import com.google.inject.Inject;
import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.exception.PostProcessException;
import com.scalar.kelpie.modules.Injector;
import com.scalar.kelpie.modules.PostProcessor;
import com.scalar.kelpie.modules.PreProcessor;
import com.scalar.kelpie.modules.Processor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import javax.annotation.concurrent.Immutable;

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
    try {
      preProcessor.execute();

      executeConcurrently();

      postProcessor.execute();

      System.out.println("The test has been completed successfully");
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
  }

  private InjectionExecutor loadInjectionExecutor() {
    try {
      Class clazz = Class.forName(config.getInjectionExecutor().get());
      Class[] types = {List.class};
      Object[] args = {injectors};

      return (InjectionExecutor) clazz.getConstructor(types).newInstance(args);
    } catch (Exception e) {
      // TODO: throw another exception
      throw new RuntimeException("Loading InjectionExecutor failed", e);
    }
  }
}
