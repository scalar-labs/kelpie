package com.scalar.kelpie.modules;

import static com.google.common.base.Preconditions.checkNotNull;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.exception.ModuleLoadException;
import com.scalar.kelpie.modules.dummy.DummyPostProcessor;
import com.scalar.kelpie.modules.dummy.DummyPreProcessor;
import com.scalar.kelpie.modules.dummy.DummyProcessor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

  private Module loadModule(String className, String classPath) throws ModuleLoadException {
    checkNotNull(className);
    checkNotNull(classPath);

    try {
      byte[] byteCode = load(classPath);

      Class<Module> clazz = (Class<Module>) defineClass(className, byteCode, 0, byteCode.length);
      Class[] types = {Config.class};
      Object[] args = {config};

      return clazz.getConstructor(types).newInstance(args);
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
