package com.scalar.kelpie.config;

import com.moandjiezana.toml.Toml;
import com.scalar.kelpie.exception.IllegalConfigException;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.concurrent.Immutable;

/** Configuration for Kelpie and your test. */
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
  private final Map<String, String> injectors = new HashMap<>();
  private boolean preProcessorEnabled = false;
  private boolean processorEnabled = false;
  private boolean postProcessorEnabled = false;
  private boolean injectorEnabled = false;

  private long concurrency = 1L;
  private long runForSec = 60L;
  private long rampForSec = 0L;

  /**
   * Constructs a {@code Config} with toml format text.
   *
   * @param tomlText text with toml format
   */
  public Config(String tomlText) {
    this(new Toml().read(tomlText));
  }

  /**
   * Constructs a {@code Config} with a toml file.
   *
   * @param tomlFile a toml file
   */
  public Config(File tomlFile) {
    this(new Toml().read(tomlFile));
  }

  /**
   * Constructs a {@code Config} with a toml object.
   *
   * @param toml a toml object
   */
  public Config(Toml toml) {
    this.toml = toml;
    loadCommon();
  }

  /**
   * Returns a toml object in the {@code Config}.
   *
   * @return {@code Toml}
   */
  public Toml getToml() {
    return toml;
  }

  /**
   * Returns a {@link com.scalar.kelpie.modules.PreProcessor} name in this {@code Config}.
   *
   * @return an {@code Optional} with a PreProcessor name
   */
  public Optional<String> getPreProcessorName() {
    return preProcessorName;
  }

  /**
   * Returns a {@link com.scalar.kelpie.modules.Processor} name in this {@code Config}.
   *
   * @return an {@code Optional} with a Processor name
   */
  public Optional<String> getProcessorName() {
    return processorName;
  }

  /**
   * Returns a {@link com.scalar.kelpie.modules.PostProcessor} name in this {@code Config}.
   *
   * @return an {@code Optional} with a PostProcessor name
   */
  public Optional<String> getPostProcessorName() {
    return postProcessorName;
  }

  /**
   * Returns a {@link com.scalar.kelpie.modules.PreProcessor} path in this {@code Config}.
   *
   * @return an {@code Optional} with a PreProcessor path
   */
  public Optional<String> getPreProcessorPath() {
    return preProcessorPath;
  }

  /**
   * Returns a {@link com.scalar.kelpie.modules.Processor} path in this {@code Config}.
   *
   * @return an {@code Optional} with a Processor path
   */
  public Optional<String> getProcessorPath() {
    return processorPath;
  }

  /**
   * Returns a {@link com.scalar.kelpie.modules.PostProcessor} path in this {@code Config}.
   *
   * @return an {@code Optional} with a PostProcessor path
   */
  public Optional<String> getPostProcessorPath() {
    return postProcessorPath;
  }

  /**
   * Returns an {@link com.scalar.kelpie.modules.Injector} map in this {@code Config}.
   *
   * @return an {@code Map} including Injector names and paths
   */
  public Map<String, String> getInjectors() {
    return injectors;
  }

  /**
   * Returns a concurrency.
   *
   * @return a concurrency
   */
  public long getConcurrency() {
    return concurrency;
  }

  /**
   * Returns a time for running a test in seconds.
   *
   * @return a runtime in seconds
   */
  public long getRunForSec() {
    return runForSec;
  }

  /**
   * Returns a ramp time before running a test in seconds.
   *
   * @return a ramp time in seconds
   */
  public long getRampForSec() {
    return rampForSec;
  }

  /**
   * Returns an {@link com.scalar.kelpie.executor.InjectionExecutor} name.
   *
   * @return an {@code Optional} with an Injector executor name
   */
  public Optional<String> getInjectionExecutor() {
    return injectionExecutor;
  }

  /**
   * Returns true if a {@link com.scalar.kelpie.modules.PreProcessor} is enabled.
   *
   * @return true if a PreProcessor is enabled
   */
  public boolean isPreProcessorEnabled() {
    return preProcessorEnabled;
  }

  /**
   * Returns true if a {@link com.scalar.kelpie.modules.Processor} is enabled.
   *
   * @return true if a Processor is enabled
   */
  public boolean isProcessorEnabled() {
    return processorEnabled;
  }

  /**
   * Returns true if a {@link com.scalar.kelpie.modules.PostProcessor} is enabled.
   *
   * @return true if a PostProcessor is enabled
   */
  public boolean isPostProcessorEnabled() {
    return postProcessorEnabled;
  }

  /**
   * Returns true if {@link com.scalar.kelpie.modules.Injector}s are enabled.
   *
   * @return true if Injectors are enabled
   */
  public boolean isInjectorEnabled() {
    return injectorEnabled;
  }

  /** Sets {@link com.scalar.kelpie.modules.PreProcessor} enable. */
  public void enablePreProcessor() {
    preProcessorEnabled = true;
  }

  /** Sets {@link com.scalar.kelpie.modules.Processor} enable. */
  public void enableProcessor() {
    processorEnabled = true;
  }

  /** Sets {@link com.scalar.kelpie.modules.PostProcessor} enable. */
  public void enablePostProcessor() {
    postProcessorEnabled = true;
  }

  /** Sets {@link com.scalar.kelpie.modules.Injector}s enable. */
  public void enableInjector() {
    injectorEnabled = true;
  }

  /** Sets all modules enable. */
  public void enableAllProcessors() {
    enablePreProcessor();
    enableProcessor();
    enablePostProcessor();
  }

  /**
   * Returns a value of the user defined variable.
   *
   * @param table a table name to specify the variable
   * @param name a variable name
   * @return {@code long} value of the variable
   */
  public long getUserLong(String table, String name) {
    return getUserLong(table, name, null);
  }

  /**
   * Returns a user defined value.
   *
   * @param table a table name to specify the variable
   * @param name a variable name
   * @param defaultValue a default value if the variable isn't defined
   * @return {@code long} value of the variable
   */
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

  /**
   * Returns a user defined value.
   *
   * @param table a table name to specify the variable
   * @param name a variable name
   * @return {@code String} value of the variable
   */
  public String getUserString(String table, String name) {
    return getUserString(table, name, null);
  }

  /**
   * Returns a user defined value.
   *
   * @param table a table name to specify the variable
   * @param name a variable name
   * @param defaultValue a default value if the variable isn't defined
   * @return {@code String} value of the variable
   */
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
