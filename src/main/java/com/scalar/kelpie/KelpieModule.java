package com.scalar.kelpie;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.exception.ModuleLoadException;
import com.scalar.kelpie.modules.Injector;
import com.scalar.kelpie.modules.ModuleLoader;
import com.scalar.kelpie.modules.PostProcessor;
import com.scalar.kelpie.modules.PreProcessor;
import com.scalar.kelpie.modules.Processor;
import java.util.List;

public class KelpieModule extends AbstractModule {
  private final Config config;
  private final ModuleLoader loader;

  public KelpieModule(Config config) {
    this.config = config;
    this.loader = new ModuleLoader(config);
  }

  @Provides
  Config provideConfig() {
    return config;
  }

  @Provides
  PreProcessor providePreProcessor() throws ModuleLoadException {
    return loader.loadPreProcessor();
  }

  @Provides
  Processor provideProcessor() throws ModuleLoadException {
    return loader.loadProcessor();
  }

  @Provides
  PostProcessor providePostProcessor() throws ModuleLoadException {
    return loader.loadPostProcessor();
  }

  @Provides
  List<Injector> provideInjectors() throws ModuleLoadException {
    return loader.loadInjectors();
  }
}
