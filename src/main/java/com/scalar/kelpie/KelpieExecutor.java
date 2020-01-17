package com.scalar.kelpie;

import com.google.inject.Inject;
import com.scalar.kelpie.modules.Injector;
import com.scalar.kelpie.modules.PostProcessor;
import com.scalar.kelpie.modules.PreProcessor;
import com.scalar.kelpie.modules.Processor;
import javax.annotation.concurrent.Immutable;

@Immutable
public class KelpieExecutor {
  private final PreProcessor preProcessor;
  private final Processor processor;
  private final PostProcessor postProcessor;
  private final Injector injector;

  @Inject
  public KelpieExecutor(
      PreProcessor preProcessor,
      Processor processor,
      PostProcessor postProcessor,
      Injector injector) {
    this.preProcessor = preProcessor;
    this.processor = processor;
    this.postProcessor = postProcessor;
    this.injector = injector;
  }

  public void execute() {
    try {
      preProcessor.execute();

      // TODO: injector.execute();
      // TODO: execute processes concurrently
      processor.execute();

      postProcessor.execute();

      System.out.println("The test has been completed successfully");
    } catch (Exception e) {
      // TODO: throw another exception
      throw new RuntimeException("The test Failed", e);
    }
  }
}
