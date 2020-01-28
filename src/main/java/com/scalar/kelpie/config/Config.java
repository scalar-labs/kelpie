package com.scalar.kelpie.config;

import com.moandjiezana.toml.Toml;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.concurrent.Immutable;

@Immutable
public class Config {
  private final Toml toml;

  private Optional<String> preProcessorName;
  private Optional<String> processorName;
  private Optional<String> postProcessorName;
  private Optional<String> preProcessorPath;
  private Optional<String> processorPath;
  private Optional<String> postProcessorPath;
  private final Map<String, String> injectors = new HashMap<String, String>();
  private boolean preProcessorEnabled = false;
  private boolean processorEnabled = false;
  private boolean postProcessorEnabled = false;
  private boolean injectorEnabled = false;

  private int concurrency = 1;
  private int runForSec = 60;
  private int rampForSec = 0;

  public Config(String tomlText) {
    this(new Toml().read(tomlText));
  }

  public Config(File tomlFile) {
    this(new Toml().read(tomlFile));
  }

  public Config(Toml toml) {
    this.toml = toml;
    loadCommon();
  }

  public Toml getToml() {
    return toml;
  }

  public Optional<String> getPreProcessorName() {
    return preProcessorName;
  }

  public Optional<String> getProcessorName() {
    return processorName;
  }

  public Optional<String> getPostProcessorName() {
    return postProcessorName;
  }

  public Optional<String> getPreProcessorPath() {
    return preProcessorPath;
  }

  public Optional<String> getProcessorPath() {
    return processorPath;
  }

  public Optional<String> getPostProcessorPath() {
    return postProcessorPath;
  }

  public Map<String, String> getInjectors() {
    return injectors;
  }

  public int getConcurrency() {
    return concurrency;
  }

  public int getRunForSec() {
    return runForSec;
  }

  public int getRampForSec() {
    return rampForSec;
  }

  public boolean isPreProcessorEnabled() {
    return preProcessorEnabled;
  }

  public boolean isProcessorEnabled() {
    return processorEnabled;
  }

  public boolean isPostProcessorEnabled() {
    return postProcessorEnabled;
  }

  public boolean isInjectorEnabled() {
    return injectorEnabled;
  }

  public void enablePreProcessor() {
    preProcessorEnabled = true;
  }

  public void enableProcessor() {
    processorEnabled = true;
  }

  public void enablePostProcessor() {
    postProcessorEnabled = true;
  }

  public void enableInjector() {
    injectorEnabled = true;
  }

  public void enableAllProcessors() {
    enablePreProcessor();
    enableProcessor();
    enablePostProcessor();
  }

  private void loadCommon() {
    Toml modules = toml.getTable("modules");
    preProcessorName = Optional.ofNullable(modules.getString("preprocessor.name"));
    processorName = Optional.ofNullable(modules.getString("processor.name"));
    postProcessorName = Optional.ofNullable(modules.getString("postprocessor.name"));
    preProcessorPath = Optional.ofNullable(modules.getString("preprocessor.path"));
    processorPath = Optional.ofNullable(modules.getString("processor.path"));
    postProcessorPath = Optional.ofNullable(modules.getString("postprocessor.path"));

    modules
        .getTables("injectors")
        .forEach(
            i -> {
              injectors.put(i.getString("name"), i.getString("path"));
            });

    Toml common = toml.getTable("common");
    if (common.getLong("concurrency") != null) {
      concurrency = new Integer(common.getLong("concurrency").toString());
    }
    if (common.getLong("run_for_sec") != null) {
      runForSec = new Integer(common.getLong("run_for_sec").toString());
    }
    if (common.getLong("run_for_sec") != null) {
      rampForSec = new Integer(common.getLong("ramp_for_sec").toString());
    }
  }
}
