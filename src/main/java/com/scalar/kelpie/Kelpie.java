package com.scalar.kelpie;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.executor.KelpieExecutor;
import java.io.File;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

@CommandLine.Command(
    description = "Execute a job built with Kelpie framework.",
    name = "kelpie",
    mixinStandardHelpOptions = true,
    version = "kelpie v0.1.0")
public class Kelpie implements Callable {
  private static final Logger LOGGER = LoggerFactory.getLogger(Kelpie.class);

  @CommandLine.Option(
      names = {"--config"},
      required = true,
      paramLabel = "<CONFIG_FILE>",
      description = "A config file of a job")
  private String configPath;

  @CommandLine.Option(
      names = {"--only-pre"},
      required = false,
      description = "Execute only the pre-process")
  private boolean onlyPre = false;

  @CommandLine.Option(
      names = {"--only-process"},
      required = false,
      description = "Execute only the process")
  private boolean onlyProcess = false;

  @CommandLine.Option(
      names = {"--only-post"},
      required = false,
      description = "Execute only the post-process")
  private boolean onlyPost = false;

  @CommandLine.Option(
      names = {"--exclude-pre"},
      required = false,
      description = "Execute without the pre-process")
  private boolean excludePre = false;

  @CommandLine.Option(
      names = {"--exclude-process"},
      required = false,
      description = "Execute without the process")
  private boolean excludeProcess = false;

  @CommandLine.Option(
      names = {"--exclude-post"},
      required = false,
      description = "Execute without the post-process")
  private boolean excludePost = false;

  @CommandLine.Option(
      names = {"--inject"},
      required = false,
      description = "Execute the injectors")
  private boolean injected = false;

  public static void main(String[] args) {
    int exitCode = new CommandLine(new Kelpie()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public Void call() {
    LOGGER.info("Checking a job config...");
    Config config = setupConfig();

    LOGGER.info("Loading modules...");
    Injector injector = Guice.createInjector(new KelpieModule(config));
    KelpieExecutor executor = injector.getInstance(KelpieExecutor.class);

    LOGGER.info("Starting the job...");
    executor.execute();

    LOGGER.info("The job has been completed successfully");

    return null;
  }

  private Config setupConfig() {
    if ((onlyPre && onlyProcess) || (onlyPre && onlyPost) || (onlyProcess && onlyPost)) {
      throw new IllegalArgumentException("You can use only one of --only-* options at once");
    }

    if ((onlyPost || onlyProcess || onlyPost) && (excludePre || excludeProcess || excludePost)) {
      throw new IllegalArgumentException("You can use either --only-* or --exclude-* option");
    }

    Config config = new Config(new File(configPath));
    if (onlyPre) {
      config.enableSelectedProcessors(true, false, false);
    } else if (onlyProcess) {
      config.enableSelectedProcessors(false, true, false);
    } else if (onlyPost) {
      config.enableSelectedProcessors(false, false, true);
    } else if (excludePre) {
      config.enableSelectedProcessors(false, true, true);
    } else if (excludeProcess) {
      config.enableSelectedProcessors(true, false, true);
    } else if (excludePost) {
      config.enableSelectedProcessors(true, true, false);
    } else {
      config.enableAllProcessors();
    }

    if (injected) {
      config.enableInjector();
    }

    return config;
  }
}
