package com.scalar.kelpie;

import static com.google.common.base.Preconditions.checkNotNull;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.modules.ModuleLoader;
import com.scalar.kelpie.modules.PostProcessor;
import com.scalar.kelpie.modules.PreProcessor;
import com.scalar.kelpie.modules.Processor;
import java.io.File;

public class KelpieExecutor {
  private final Config config;
  private final boolean doPre;
  private final boolean doProcess;
  private final boolean doPost;
  private PreProcessor preProcessor;
  private Processor processor;
  private PostProcessor postProcessor;

  public KelpieExecutor(String configPath, boolean doPre, boolean doProcess, boolean doPost) {
    checkNotNull(configPath);
    this.config = new Config(new File(configPath));
    this.doPre = doPre;
    this.doProcess = doProcess;
    this.doPost = doPost;
  }

  public void execute() {
    loadAllModules(config);

    if (doPre) {
      preProcessor.preProcess();
    }

    // TODO: injector
    // TODO: execute processes concurrently
    if (doProcess) {
      processor.process();
    }

    boolean result = true;
    if (doPost) {
      result = postProcessor.postProcess();
    }

    if (result) {
      System.out.println("The test has been completed successfully");
    } else {
      // TODO: throw another exception
      throw new RuntimeException("The test Failed");
    }
  }

  private void loadAllModules(Config config) {
    ModuleLoader loader = new ModuleLoader(config);
    try {
      if (doPre) {
        preProcessor = loader.loadPreProcessor();
      }
      if (doProcess) {
        processor = loader.loadProcessor();
      }
      if (doPost) {
        postProcessor = loader.loadPostProcessor();
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to load modules.", e);
    }
  }
}
