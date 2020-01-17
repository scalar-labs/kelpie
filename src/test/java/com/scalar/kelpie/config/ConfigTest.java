package com.scalar.kelpie.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.util.Optional;
import org.junit.Test;

public class ConfigTest {
  static final String ANY_PRE_NAME = "my.PreProcessor";
  static final String ANY_PRE_PATH = "/path/to/PreProcessor";
  static final Long ANY_CONCURRENCY = 8L;
  static final String ANY_ADDITIONAL_CONFIG = "my_config";
  static final String ANY_PARAMETER = "my_parameter";
  static final Long ANY_VALUE = 100L;
  static final Long DEFAULT_RUN_FOR_SEC = 60L;
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
          + "[modules.injector]\n"
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
  public void getConcurrency_ShouldGetProperly() {
    // Arrange
    Config config = new Config(tomlText);

    // Act
    Long concurrency = config.getConcurrency();

    // Assert
    assertThat(concurrency).isEqualTo(ANY_CONCURRENCY);
  }

  @Test
  public void getRunForSec_NoValueGiven_ShouldGetDefaultValue() {
    // Arrange
    Config config = new Config(tomlText);

    // Act
    Long runForSec = config.getRunForSec();

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
