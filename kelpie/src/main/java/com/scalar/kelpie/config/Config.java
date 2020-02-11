package com.scalar.kelpie.config;

import com.moandjiezana.toml.Toml;
import com.scalar.kelpie.exception.IllegalConfigException;
import java.io.File;
import java.util.ArrayList;
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
  private Optional<String> preProcessorClassPath = Optional.empty();
  private Optional<String> processorClassPath = Optional.empty();
  private Optional<String> postProcessorClassPath = Optional.empty();
  private final Map<String, String> injectors = new HashMap<>();
  private final List<String> injectorClassPaths = new ArrayList<>();
  private boolean preProcessorEnabled = false;
  private boolean processorEnabled = false;
  private boolean postProcessorEnabled = false;
  private boolean injectorEnabled = false;

  private long concurrency = 1L;
  private long runForSec = 60L;
  private long rampForSec = 0L;

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

  public Optional<String> getPreProcessorClassPath() {
    return preProcessorClassPath;
  }

  public Optional<String> getProcessorClassPath() {
    return processorClassPath;
  }

  public Optional<String> getPostProcessorClassPath() {
    return postProcessorClassPath;
  }

  public List<String> getInjectorClassPaths() {
    return injectorClassPaths;
  }

  public long getConcurrency() {
    return concurrency;
  }

  public long getRunForSec() {
    return runForSec;
  }

  public long getRampForSec() {
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

  public long getUserLong(String table, String name) {
    return getUserLong(table, name, null);
  }

  public long getUserLong(String table, String name, Long defaultValue) {
    final Toml t;
    try {
      t = getTable(table);
    } catch (IllegalConfigException e) {
      if (defaultValue != null) {
        return defaultValue;
      } else {
        throw e;
      }
    }

    Long v = t.getLong(name);
    if (v != null) {
      return v;
    } else if (defaultValue != null) {
      return defaultValue;
    } else {
      throw new IllegalConfigException(table + "." + name + " doesn't exist");
    }
  }

  public String getUserString(String table, String name) {
    return getUserString(table, name, null);
  }

  public String getUserString(String table, String name, String defaultValue) {
    final Toml t;
    try {
      t = getTable(table);
    } catch (IllegalConfigException e) {
      if (defaultValue != null) {
        return defaultValue;
      } else {
        throw e;
      }
    }

    String str = t.getString(name);
    if (str != null) {
      return str;
    } else if (defaultValue != null) {
      return defaultValue;
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
      preProcessorClassPath = Optional.ofNullable(modules.getString("preprocessor.classpath"));
      processorClassPath = Optional.ofNullable(modules.getString("processor.classpath"));
      postProcessorClassPath = Optional.ofNullable(modules.getString("postprocessor.classpath"));

      List<Toml> injectorsTable = modules.getTables("injectors");
      if (injectorsTable != null) {
        injectorsTable.forEach(
            i -> {
              injectors.put(i.getString("name"), i.getString("path"));
              if (i.getString("classpath") != null) {
                injectorClassPaths.add(i.getString("classpath"));
              }
            });
      }
    }

    Toml common = toml.getTable("common");
    if (common.getLong("concurrency") != null) {
      concurrency = common.getLong("concurrency");
      if (concurrency <= 0) {
        throw new IllegalConfigException("common.concurrency should be positive");
      }
    }
    if (common.getLong("run_for_sec") != null) {
      runForSec = common.getLong("run_for_sec");
      if (runForSec <= 0) {
        throw new IllegalConfigException("common.run_for_sec should be positive");
      }
    }
    if (common.getLong("run_for_sec") != null) {
      rampForSec = common.getLong("ramp_for_sec");
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
