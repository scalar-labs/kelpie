package com.scalar.kelpie.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.scalar.kelpie.exception.IllegalConfigException;
import java.io.File;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;

public class ConfigTest {
  static final String ANY_PRE_NAME = "my.PreProcessor";
  static final String ANY_PRE_PATH = "/path/to/PreProcessor";
  static final String ANY_INJECTOR1_NAME = "my.Injector1";
  static final String ANY_INJECTOR1_PATH = "/path/to/Injectro1";
  static final String ANY_INJECTOR2_NAME = "my.Injector2";
  static final String ANY_INJECTOR2_PATH = "/path/to/Injector2";
  static final String ANY_INECTION_EXECUTOR = "com.scalar.kelpie.executor.Test";
  static final int ANY_CONCURRENCY = 8;
  static final String MY_CONFIG_TABLE = "my_config";
  static final String WRONG_CONFIG_TABLE = "no_config";
  static final String INT_PARAMETER = "my_int_parameter";
  static final String STRING_PARAMETER = "my_str_parameter";
  static final String WRONG_PARAMETER = "no_parameter";
  static final int ANY_INT = 100;
  static final String ANY_STRING = "test";

  static final int DEFAULT_RUN_FOR_SEC = 60;
  static final String WRONG_FILE = "/path/to/config.toml";

  static final String tomlText =
      "[modules]\n"
          + "[modules.preprocessor]\n"
          + "  name = \""
          + ANY_PRE_NAME
          + "\"\n"
          + "  path = \""
          + ANY_PRE_PATH
          + "\"\n"
          + "[modules.processor]\n"
          + "[modules.postprocessor]\n"
          + "[[modules.injectors]]\n"
          + "  name = \""
          + ANY_INJECTOR1_NAME
          + "\"\n"
          + "  path = \""
          + ANY_INJECTOR1_PATH
          + "\"\n"
          + "[[modules.injectors]]\n"
          + "  name = \""
          + ANY_INJECTOR2_NAME
          + "\"\n"
          + "  path = \""
          + ANY_INJECTOR2_PATH
          + "\"\n"
          + "[common]\n"
          + "  concurrency = "
          + ANY_CONCURRENCY
          + "\n"
          + "["
          + MY_CONFIG_TABLE
          + "]\n"
          + INT_PARAMETER
          + " = "
          + ANY_INT
          + "\n"
          + STRING_PARAMETER
          + " = \""
          + ANY_STRING
          + "\"\n";

  @Test
  public void getPreProcessorName_ShouldGetProperly() {
    // Arrange
    Config config = new Config(tomlText);

    // Act
    Optional<String> name = config.getPreProcessorName();

    // Assert
    assertThat(name.get()).isEqualTo(ANY_PRE_NAME);
  }

  @Test
  public void getProcessorName_NoNaveGiven_ShouldNotGet() {
    // Arrange
    Config config = new Config(tomlText);

    // Act
    Optional<String> name = config.getProcessorName();

    // Assert
    assertThat(name.isPresent()).isFalse();
  }

  @Test
  public void getInjectorNameAndPath_ShouldGetProperly() {
    // Arrange
    Config config = new Config(tomlText);

    // Act
    Map<String, String> injectors = config.getInjectors();

    // Assert
    assertThat(injectors.get(ANY_INJECTOR1_NAME)).isEqualTo(ANY_INJECTOR1_PATH);
    assertThat(injectors.get(ANY_INJECTOR2_NAME)).isEqualTo(ANY_INJECTOR2_PATH);
  }

  @Test
  public void getConcurrency_ShouldGetProperly() {
    // Arrange
    Config config = new Config(tomlText);

    // Act
    int concurrency = config.getConcurrency();

    // Assert
    assertThat(concurrency).isEqualTo(ANY_CONCURRENCY);
  }

  @Test
  public void getConcurrency_NegativeValueGiven_ShouldThrowIllegalConfigException() {
    // Act Assert
    assertThatThrownBy(
            () -> {
              new Config("[common]\n" + "concurrency = -1");
            })
        .isInstanceOf(IllegalConfigException.class);
  }

  @Test
  public void getRunForSec_NoValueGiven_ShouldGetDefaultValue() {
    // Arrange
    Config config = new Config(tomlText);

    // Act
    int runForSec = config.getRunForSec();

    // Assert
    assertThat(runForSec).isEqualTo(DEFAULT_RUN_FOR_SEC);
  }

  @Test
  public void getInjectionExecutor_ShouldGetDefaultExecutor() {
    // Arrange
    Config config = new Config(tomlText);

    // Act
    String executor = config.getInjectionExecutor().get();

    // Assert
    assertThat(executor).isEqualTo("com.scalar.kelpie.executor.RandomInjectionExecutor");
  }

  @Test
  public void getInjectionExecutor_ShouldGetProperly() {
    // Arrange
    Config config = new Config("[common]\n" + "injection_executor = \"" + ANY_INECTION_EXECUTOR + "\"");

    // Act
    String executor = config.getInjectionExecutor().get();

    // Assert
    assertThat(executor).isEqualTo(ANY_INECTION_EXECUTOR);
  }

  @Test
  public void getUserInteger_ShouldGetProperly() {
    // Arrange
    Config config = new Config(tomlText);

    // Act
    int parameter = config.getUserInteger(MY_CONFIG_TABLE, INT_PARAMETER);

    // Assert
    assertThat(parameter).isEqualTo(ANY_INT);
  }

  @Test
  public void getUserString_ShouldGetProperly() {
    // Arrange
    Config config = new Config(tomlText);

    // Act
    String parameter = config.getUserString(MY_CONFIG_TABLE, STRING_PARAMETER);

    // Assert
    assertThat(parameter).isEqualTo(ANY_STRING);
  }

  @Test
  public void getUserInteger_NonExistTableGiven_ShouldThrowIllegalConfigException() {
    // Arrange
    Config config = new Config(tomlText);

    // Act Assert
    assertThatThrownBy(
            () -> {
              config.getUserInteger(WRONG_CONFIG_TABLE, INT_PARAMETER);
            })
        .isInstanceOf(IllegalConfigException.class);
  }

  @Test
  public void getUserInteger_NonExistParameterGiven_ShouldThrowIllegalConfigException() {
    // Arrange
    Config config = new Config(tomlText);

    // Act Assert
    assertThatThrownBy(
            () -> {
              config.getUserInteger(MY_CONFIG_TABLE, WRONG_PARAMETER);
            })
        .isInstanceOf(IllegalConfigException.class);
  }

  @Test
  public void getUserString_NonExistParameterGiven_ShouldThrowIllegalConfigException() {
    // Arrange
    Config config = new Config(tomlText);

    // Act Assert
    assertThatThrownBy(
            () -> {
              config.getUserString(MY_CONFIG_TABLE, WRONG_PARAMETER);
            })
        .isInstanceOf(IllegalConfigException.class);
   }

  @Test
  public void constructor_ShouldThrowRuntimeException() {
    // Act Assert
    assertThatThrownBy(
            () -> {
              new Config(new File(WRONG_FILE));
            })
        .isInstanceOf(RuntimeException.class);
  }
}
