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

  public ModuleLoader(Config config) {
    this.config = config;
  }

  public PreProcessor loadPreProcessor() throws ModuleLoadException {
    if (config.isPreProcessorEnabled()) {
      return (PreProcessor)
          loadModule(config.getPreProcessorName().get(), config.getPreProcessorPath().get());
    } else {
      return new DummyPreProcessor(config);
    }
  }

  public Processor loadProcessor() throws ModuleLoadException {
    if (config.isProcessorEnabled()) {
      return (Processor)
          loadModule(config.getProcessorName().get(), config.getProcessorPath().get());
    } else {
      return new DummyProcessor(config);
    }
  }

  public PostProcessor loadPostProcessor() throws ModuleLoadException {
    if (config.isPostProcessorEnabled()) {
      return (PostProcessor)
          loadModule(config.getPostProcessorName().get(), config.getPostProcessorPath().get());
    } else {
      return new DummyPostProcessor(config);
    }
  }

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
      URLClassLoader classLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
      URL jarUrl = new File(jarPath).toURI().toURL();
      Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
      method.setAccessible(true);
      method.invoke(classLoader, jarUrl);

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
