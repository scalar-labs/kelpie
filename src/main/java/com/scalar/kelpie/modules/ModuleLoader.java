package com.scalar.kelpie.modules;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.exception.ModuleLoadException;
import com.scalar.kelpie.modules.dummy.DummyPostProcessor;
import com.scalar.kelpie.modules.dummy.DummyPreProcessor;
import com.scalar.kelpie.modules.dummy.DummyProcessor;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * ModuleLoader loads modules. If a module isn't specified in the config, ModuleLoader loads a dummy
 * module for {@link PreProcessor}, {@link Processor} and {@link PostProcessor}.
 */
public class ModuleLoader {
  private Config config;

  /**
   * Creates a ModuleLoader.
   *
   * @param config configuration object
   */
  public ModuleLoader(Config config) {
    this.config = config;
  }

  /**
   * Loads the pre-processor module specified in the configuration.
   * @return the loaded {@link PreProcessor}
   * @throws ModuleLoadException if the module fails to load
   */
  public PreProcessor loadPreProcessor() throws ModuleLoadException {
    if (config.isPreProcessorEnabled()) {
      return (PreProcessor)
          loadModule(config.getPreProcessorName().get(), config.getPreProcessorPath().get());
    } else {
      return new DummyPreProcessor(config);
    }
  }

  /**
   * Loads a Processor object
   * @return Processor object
   * @throws ModuleLoadException object
   */
  public Processor loadProcessor() throws ModuleLoadException {
    if (config.isProcessorEnabled()) {
      return (Processor)
          loadModule(config.getProcessorName().get(), config.getProcessorPath().get());
    } else {
      return new DummyProcessor(config);
    }
  }

  /**
   * Loads a PostProcessor object
   * @return PostProcessor object
   * @throws ModuleLoadException object
   */
  public PostProcessor loadPostProcessor() throws ModuleLoadException {
    if (config.isPostProcessorEnabled()) {
      return (PostProcessor)
          loadModule(config.getPostProcessorName().get(), config.getPostProcessorPath().get());
    } else {
      return new DummyPostProcessor(config);
    }
  }

/**
 * Loads and initializes all configured {@link Injector} modules.
 *
 * <p>If injector support is enabled in the configuration, this method
 * iterates through the configured injector definitions, dynamically
 * loads each module, and returns them as a list.</p>
 *
 * <p>If injectors are disabled, an empty list is returned.</p>
 *
 * @return a list of successfully loaded {@link Injector} instances;
 *         never {@code null}
 * @throws ModuleLoadException if any configured injector module
 */
public List<Injector> loadInjectors() throws ModuleLoadException {
    List<Injector> injectors = new ArrayList<>();

    if (config.isInjectorEnabled()) {
      config
          .getInjectors()
          .forEach(
              (name, path) -> {
                injectors.add((Injector) loadModule(name, path));
              });
    }

    return injectors;
  }

  private Module loadModule(String className, String jarPath) throws ModuleLoadException {
    try {
      URL jarUrl = new File(jarPath).toURI().toURL();
      URLClassLoader classLoader = new URLClassLoader(new URL[] { jarUrl }, Thread.currentThread().getContextClassLoader());
      Thread.currentThread().setContextClassLoader(classLoader);

      @SuppressWarnings("unchecked")
      Class<Module> clazz = (Class<Module>) Class.forName(className, true, classLoader);
      Class<?>[] types = {Config.class};
      Object[] args = {config};
      return clazz.getConstructor(types).newInstance(args);
    } catch (Exception e) {
      throw new ModuleLoadException("Failed to load a module " + className + " from " + jarPath, e);
    }
  }
}
