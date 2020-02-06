package com.scalar.kelpie.config;

import com.moandjiezana.toml.Toml;
import com.scalar.kelpie.exception.IllegalConfigException;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.concurrent.Immutable;

@Immutable
public class Config {
  private final String DEFAULT_INJECTION_EXECUTOR =
      "com.scalar.kelpie.executor.RandomInjectionExecutor";
  private final Toml toml;

  private Optional<String> preProcessorName = Optional.empty();
  private Optional<String> processorName = Optional.empty();
  private Optional<String> postProcessorName = Optional.empty();
  private Optional<String> preProcessorPath = Optional.empty();
  private Optional<String> processorPath = Optional.empty();
  private Optional<String> postProcessorPath = Optional.empty();
  private Optional<String> injectionExecutor = Optional.empty();
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

  public Optional<String> getInjectionExecutor() {
    return injectionExecutor;
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

  public int getUserInteger(String table, String name) {
    Long v = getTable(table).getLong(name);
    if (v != null) {
      return new Integer(v.toString());
    } else {
      throw new IllegalConfigException(table + "." + name + " doesn't exist");
    }
  }

  public String getUserString(String table, String name) {
    String str = getTable(table).getString(name);
    if (str != null) {
      return str;
    } else {
      throw new IllegalConfigException(table + "." + name + " doesn't exist");
    }
  }

  private Toml getTable(String table) {
    Toml t = toml.getTable(table);
    if (t == null) {
      throw new IllegalConfigException(table + " doesn't exist");
    }

    return t;
  }

  private void loadCommon() {
    Toml modules = toml.getTable("modules");
    if (modules != null) {
      preProcessorName = Optional.ofNullable(modules.getString("preprocessor.name"));
      processorName = Optional.ofNullable(modules.getString("processor.name"));
      postProcessorName = Optional.ofNullable(modules.getString("postprocessor.name"));
      preProcessorPath = Optional.ofNullable(modules.getString("preprocessor.path"));
      processorPath = Optional.ofNullable(modules.getString("processor.path"));
      postProcessorPath = Optional.ofNullable(modules.getString("postprocessor.path"));

      List<Toml> injectorsTable = modules.getTables("injectors");
      if (injectorsTable != null) {
        injectorsTable.forEach(
            i -> {
              injectors.put(i.getString("name"), i.getString("path"));
            });
      }
    }

    Toml common = toml.getTable("common");
    if (common.getLong("concurrency") != null) {
      concurrency = new Integer(common.getLong("concurrency").toString());
      if (concurrency <= 0) {
        throw new IllegalConfigException("common.concurrency should be positive");
      }
    }
    if (common.getLong("run_for_sec") != null) {
      runForSec = new Integer(common.getLong("run_for_sec").toString());
      if (runForSec <= 0) {
        throw new IllegalConfigException("common.run_for_sec should be positive");
      }
    }
    if (common.getLong("run_for_sec") != null) {
      rampForSec = new Integer(common.getLong("ramp_for_sec").toString());
      if (rampForSec <= 0) {
        throw new IllegalConfigException("common.ramp_for_sec should be positive");
      }
    }
    if (common.getString("injection_executor") != null) {
      injectionExecutor = Optional.of(common.getString("injection_executor"));
    } else {
      injectionExecutor = Optional.of(DEFAULT_INJECTION_EXECUTOR);
    }
  }
}
