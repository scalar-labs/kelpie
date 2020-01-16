package com.scalar.kelpie.config;

import com.moandjiezana.toml.Toml;
import java.io.File;
import java.util.Optional;
import javax.annotation.concurrent.Immutable;

@Immutable
public class Config {
  private final Toml toml;

  private Optional<String> preProcessorName;
  private Optional<String> processorName;
  private Optional<String> postProcessorName;
  private Optional<String> injectorName;
  private Optional<String> preProcessorPath;
  private Optional<String> processorPath;
  private Optional<String> postProcessorPath;
  private Optional<String> injectorPath;

  private Long concurrency = 1L;
  private Long runForSec = 60L;
  private Long rampForSec = 0L;

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

  public Optional<String> getInjectorName() {
    return injectorName;
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

  public Optional<String> getInjectorPath() {
    return injectorPath;
  }

  public Long getConcurrency() {
    return concurrency;
  }

  public Long getRunForSec() {
    return runForSec;
  }

  public Long getRampForSet() {
    return rampForSec;
  }

  private void loadCommon() {
    Toml modules = toml.getTable("modules");
    preProcessorName = Optional.ofNullable(modules.getString("preprocessor.name"));
    processorName = Optional.ofNullable(modules.getString("processor.name"));
    postProcessorName = Optional.ofNullable(modules.getString("postprocessor.name"));
    injectorName = Optional.ofNullable(modules.getString("injector.name"));
    preProcessorPath = Optional.ofNullable(modules.getString("preprocessor.path"));
    processorPath = Optional.ofNullable(modules.getString("processor.path"));
    postProcessorPath = Optional.ofNullable(modules.getString("postprocessor.path"));
    injectorPath = Optional.ofNullable(modules.getString("injector.path"));

    Toml common = toml.getTable("common");
    if (common.getLong("concurrency") != null) {
      concurrency = common.getLong("concurrency");
    }
    if (common.getLong("run_for_sec") != null) {
      runForSec = common.getLong("run_for_sec");
    }
    if (common.getLong("run_for_sec") != null) {
      rampForSec = common.getLong("ramp_for_sec");
    }
  }
}
