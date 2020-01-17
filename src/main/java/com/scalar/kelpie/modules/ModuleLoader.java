package com.scalar.kelpie.modules;

import static com.google.common.base.Preconditions.checkNotNull;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.exception.ModuleLoadException;
import com.scalar.kelpie.modules.dummy.DummyInjector;
import com.scalar.kelpie.modules.dummy.DummyPostProcessor;
import com.scalar.kelpie.modules.dummy.DummyPreProcessor;
import com.scalar.kelpie.modules.dummy.DummyProcessor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ModuleLoader extends ClassLoader {
  private Config config;

  public ModuleLoader(Config config) {
    this.config = config;
  }

  public PreProcessor loadPreProcessor() throws ModuleLoadException {
    if (config.isPreProcessorEnabled()) {
      return (PreProcessor)
          loadModule(config.getPreProcessorName().get(), config.getPreProcessorPath().get());
    } else {
      return new DummyPreProcessor();
    }
  }

  public Processor loadProcessor() throws ModuleLoadException {
    if (config.isProcessorEnabled()) {
      return (Processor)
          loadModule(config.getProcessorName().get(), config.getProcessorPath().get());
    } else {
      return new DummyProcessor();
    }
  }

  public PostProcessor loadPostProcessor() throws ModuleLoadException {
    if (config.isPostProcessorEnabled()) {
      return (PostProcessor)
          loadModule(config.getPostProcessorName().get(), config.getPostProcessorPath().get());
    } else {
      return new DummyPostProcessor();
    }
  }

  public Injector loadInjector() throws ModuleLoadException {
    if (config.isInjectorEnabled()) {
      return (Injector) loadModule(config.getInjectorName().get(), config.getInjectorPath().get());
    } else {
      return new DummyInjector();
    }
  }

  private Module loadModule(String className, String classPath) throws ModuleLoadException {
    checkNotNull(className);
    checkNotNull(classPath);

    try {
      byte[] byteCode = load(classPath);

      Class<Module> clazz = (Class<Module>) defineClass(className, byteCode, 0, byteCode.length);

      Module module = clazz.getConstructor().newInstance();
      module.initialize(config);
      return module;
    } catch (Exception e) {
      throw new ModuleLoadException(
          "Failed to load a module " + className + " from " + classPath, e);
    }
  }

  private byte[] load(String path) throws IOException {
    File file = new File(path);
    byte[] bytes = new byte[(int) file.length()];
    try (FileInputStream stream = new FileInputStream(file)) {
      stream.read(bytes, 0, bytes.length);
    }
    return bytes;
  }
}
