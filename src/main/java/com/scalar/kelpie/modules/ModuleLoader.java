package com.scalar.kelpie.modules;

import static com.google.common.base.Preconditions.checkNotNull;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.exception.ModuleLoadException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ModuleLoader extends ClassLoader {
  private Config config;

  public ModuleLoader(Config config) {
    this.config = config;
  }

  public PreProcessor loadPreProcessor() throws ModuleLoadException {
    return (PreProcessor)
        loadModule(config.getPreProcessorName().get(), config.getPreProcessorPath().get());
  }

  public Processor loadProcessor() throws ModuleLoadException {
    return (Processor)
        loadModule(config.getProcessorName().get(), config.getProcessorPath().get());
  }

  public PostProcessor loadPostProcessor() throws ModuleLoadException {
    return (PostProcessor)
        loadModule(config.getPostProcessorName().get(), config.getPostProcessorPath().get());
  }

  public Injector loadInjector() throws ModuleLoadException {
    return (Injector)
        loadModule(config.getInjectorName().get(), config.getInjectorPath().get());
  }

  private Module loadModule(String className, String classPath) throws ModuleLoadException {
    try {
      checkNotNull(className);
      checkNotNull(classPath);

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
