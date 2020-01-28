package com.scalar.kelpie;

import com.google.inject.Inject;
import com.scalar.kelpie.modules.Injector;
import com.scalar.kelpie.modules.PostProcessor;
import com.scalar.kelpie.modules.PreProcessor;
import com.scalar.kelpie.modules.Processor;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import javax.annotation.concurrent.Immutable;

@Immutable
public class KelpieExecutor {
  private final PreProcessor preProcessor;
  private final Processor processor;
  private final PostProcessor postProcessor;
  private final List<Injector> injectors;
  private final Random random;
  private final AtomicBoolean isDone;

  @Inject
  public KelpieExecutor(
      PreProcessor preProcessor,
      Processor processor,
      PostProcessor postProcessor,
      List<Injector> injectors) {
    this.preProcessor = preProcessor;
    this.processor = processor;
    this.postProcessor = postProcessor;
    this.injectors = injectors;

    this.random = new Random(System.currentTimeMillis());
    this.isDone = new AtomicBoolean(false);
  }

  public void execute() {
    try {
      preProcessor.execute();

      executeConcurrently();

      postProcessor.execute();

      System.out.println("The test has been completed successfully");
    } catch (Exception e) {
      // TODO: throw another exception
      throw new RuntimeException("The test Failed", e);
    }
  }

  private void executeConcurrently() {
    int concurrency = processor.getConfig().getConcurrency();
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
    CompletableFuture<Void> injectorFuture =
        CompletableFuture.runAsync(
            () -> {
              executeRandomInjection();
            },
            es);

    // Wait for completion of all processor.execute()
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();

    // Stop the injector gracefully
    isDone.set(true);

    // Wait for completion of the injectors
    injectorFuture.join();
  }

  private void executeRandomInjection() {
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
