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

public class ModuleLoader extends ClassLoader {
  private Config config;

  public ModuleLoader(Config config) {
    this.config = config;
  }

  public PreProcessor loadPreProcessor() throws ModuleLoadException {
    if (config.isPreProcessorEnabled()) {
      if (config.getPreProcessorClassPath().isPresent()) {
        addClassPath(config.getPreProcessorClassPath().get());
      }
      return (PreProcessor)
          loadModule(config.getPreProcessorName().get(), config.getPreProcessorPath().get());
    } else {
      return new DummyPreProcessor(config);
    }
  }

  public Processor loadProcessor() throws ModuleLoadException {
    if (config.isProcessorEnabled()) {
      if (config.getProcessorClassPath().isPresent()) {
        addClassPath(config.getProcessorClassPath().get());
      }
      return (Processor)
          loadModule(config.getProcessorName().get(), config.getProcessorPath().get());
    } else {
      return new DummyProcessor(config);
    }
  }

  public PostProcessor loadPostProcessor() throws ModuleLoadException {
    if (config.isPostProcessorEnabled()) {
      if (config.getPostProcessorClassPath().isPresent()) {
        addClassPath(config.getPostProcessorClassPath().get());
      }
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
          .getInjectorClassPaths()
          .forEach(
              cp -> {
                addClassPath(cp);
              });
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
      URL[] urls = new URL[] {new File(jarPath).toURL()};
      ClassLoader loader = URLClassLoader.newInstance(urls, getClass().getClassLoader());
      Class<Module> clazz = (Class<Module>) Class.forName(className, true, loader);
      Class[] types = {Config.class};
      Object[] args = {config};

      return clazz.getConstructor(types).newInstance(args);
    } catch (Exception e) {
      throw new ModuleLoadException("Failed to load a module " + className + " from " + jarPath, e);
    }
  }

  private void addClassPath(String classPath) {
    File cp = new File(classPath);
    if (!cp.isDirectory()) {
      addPath(cp);
    } else {
      for (File f : cp.listFiles()) {
        addPath(f);
      }
    }
  }

  private void addPath(File f) {
    try {
      URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
      Class<URLClassLoader> urlClass = URLClassLoader.class;
      Method method = urlClass.getDeclaredMethod("addURL", new Class[] {URL.class});
      method.setAccessible(true);
      method.invoke(urlClassLoader, new Object[] {f.toURL()});
    } catch (Exception e) {
      throw new ModuleLoadException("Failed to add the classpath " + f, e);
    }
  }
}
