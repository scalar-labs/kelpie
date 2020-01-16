package com.scalar.kelpie;

import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(
    description = "Execute a test built with Kelpie framework.",
    name = "kelpie",
    mixinStandardHelpOptions = true,
    version = "kelpie v0.1.0")
public class Kelpie implements Callable {

  @CommandLine.Option(
      names = {"--config"},
      required = true,
      paramLabel = "<CONFIG_FILE>",
      description = "A config file of a test")
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

  public static void main(String[] args) {
    int exitCode = new CommandLine(new Kelpie()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public Void call() {
    boolean doPre = true;
    boolean doProcess = true;
    boolean doPost = true;

    if ((onlyPre && onlyProcess) || (onlyPre && onlyPost) || (onlyProcess && onlyPost)) {
      throw new IllegalArgumentException("You can use only one of --only-* options at once");
    }

    if (onlyPre) {
      doProcess = false;
      doPost = false;
    }
    if (onlyProcess) {
      doPre = false;
      doPost = false;
    }
    if (onlyPost) {
      doPre = false;
      doProcess = false;
    }

    KelpieExecutor executor = new KelpieExecutor(configPath, doPre, doProcess, doPost);
    executor.execute();

    return null;
  }
}
