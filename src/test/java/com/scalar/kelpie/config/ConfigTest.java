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
  static final int ANY_CONCURRENCY = 8;
  static final String ANY_ADDITIONAL_CONFIG = "my_config";
  static final String ANY_PARAMETER = "my_parameter";
  static final Long ANY_VALUE = 100L;
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
          + ANY_ADDITIONAL_CONFIG
          + "]\n"
          + ANY_PARAMETER
          + " = "
          + ANY_VALUE
          + "\n";

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
  public void getMyConfigValue_ShouldGetProperly() {
    // Arrange
    Config config = new Config(tomlText);

    // Act
    Long parameter = config.getToml().getTable(ANY_ADDITIONAL_CONFIG).getLong(ANY_PARAMETER);

    // Assert
    assertThat(parameter).isEqualTo(ANY_VALUE);
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
