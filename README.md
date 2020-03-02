![](https://github.com/scalar-labs/kelpie/workflows/Kelpie/badge.svg)

# Kelpie

Kelpie is a simple yet general framework for running end-to-end testing such as system verification and benchmarking.

## How Kelpie works
Kelpie is composed of a framework that orchestrates a test and takes care of task management such as concurrent execution, and a test that is run by the framework.
As the following diagram, a test in Kelpie has 3 steps; pre-processing, processing and post-processing, which run in a sequential order. A test can also have an injection step that runs in parallel with the processing step. The behavior of each step can be described by implementing the corresponding modules called `PreProcessor`, `Processor`, `PostProcessor` and `Injector` respectively.

<p align="center">
  <img src="doc/kelpie.png" width=450px>
  <br>
  Kelpie overview
</p>

# Usage
## Build Kelpie
```
./gradlew installDist
```
  - Of course, you can archive Kelpie jar and libraries by `distZip` and so on.

## Build your modules
1. The first thing to do to run your test with Kelpie, you need to create your own modules
    - Note that you don't need to create all 4 modules but you need at least one module. Please refer to [example modules](print-modules/), which do trivial printing work.
2. Build them
    ```
    ./gradlew shadowJar
    ```
    - Each module should be built to a fat JAR file including libraries that the module depends on.

## Run your test
1. Prepare a configuration file
    - A configuration file for Kelpie requires at least the locations of modules to run. Additionally, you can define static variables to pass to modules in the file. Please refer to an example configuration file in `print-modules/config.toml` for more detail.
2. Run a test
    ```
    kelpie/build/install/kelpie/bin/kelpie --config your_config.toml
    ```
    - There are other options such as `--only-pre`, `--only-process` and `--only-post`, which run only the specified step.

# How to create your own modules
Let's take a closer look at each module to properly write your own modules.

## PreProcessor
`PreProcessor` runs some tasks before `Processor`. It is usually used for some preparation of the subsequent processing. For example, it can populate initial records for a database performance benchmarking.
`PreProcessor` has one method called `execute` where you can define its behavior. `execute` can be non-thread-safe since it is executed by a single thread.

The following is `PrintPre` class from the example print modules, which does nothing except for printing some texts to stdout. As you can see, you can write arbitrary code in the `execute` method. Also, you can pass some static variables to the method through `Config` that is instantiated based on a configuration file (`print-modules/config.toml` for the print-modules case).

```java
package print;

import com.scalar.kelpie.modules.PreProcessor;

public class PrintPre extends PreProcessor {

  public PrintPre(Config config) {
    super(config);
  }

  @Override
  public void execute() {
    String title = config.getUserString("print_test", "title");

    System.out.println("Prepare for " + title);
  }
}
```

## Processor
`Processor` executes the main process. For example, `Processor` makes and sends requests to the server.

Like `Preprocessor`, you have to implement the constructor and `execute()`. `execute()` might be executed concurrently on multiple threads. That's why you need to make `execute()` thread-safe. If you don't want to do `execute()` concurrently, you have to set `concurrency = 1` in the config file.

The following class is a sample of `Processor`. It gets a number of `num` from `config`, counts the number and waits for a second. If you set `concurrency` more than 1, these counting messages are output by each thread.

```java
package print;

import com.scalar.kelpie.modules.Processor;

public class PrintProcessor extends Processor {

  public PrintProcessor(Config config) {
    super(config);
  }

  @Override
  public void execute() {
    long num = config.getUserLong("print_test", "num");

    for (long i = 0; i < num; i++) {
      try {
        long id = Thread.currentThread().getId();
        System.out.println("[thread " + id + "] Runnning... " + i);
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // ignore
      }
    }
  }
}
```

## PostProcessor
`PostProcessor` executes something after all `Processor#execute()` finish. For example, `PostProcessor` reads all records of the database and checks if their values are as expected. `PostProcessor#execute()` is always executed on a single thread.

Like `PreProcessor` and `Processor`, you have to implement the constructor and `execute()`. The following class is a sample of `PostProcessor` to just print the summary. It gets the parameters `title` and `num` from `config` and prints the summary.

```java
package print;

import com.scalar.kelpie.modules.PostProcessor;

public class PrintPost extends PostProcessor {

  public PrintPost(Config config) {
    super(config);
  }

  @Override
  public void execute() {
    String = config.getUserString("print_test", "title");
    int num = config.getUserLong("print_test", "num");

    System.out.println("Checking for " + title);
    System.out.println("Run for " + num + " seconds");

    // always succeed
  }
}
```

If you want `PostProcessor` to check the result of `Processor` execution, `PostProcessor#execute()` might need to throw an exception for the unexpected result. A test will fail when an exception is thrown from each module.

## Injector
`Injector` is different from other modules. It is executed while `Processor#execute()` is running. For example, `Injector` kills and restarts a database process for distructive testing.

You execute `Injector` by `--inject` option when you start a test with Kelpie.

```bash
$ ./kelpie --config my_config.toml --inject
```

The following class is a sample of `Injector`. You have to implement `inject()` and `eject()`. Kelpie always executes `eject()` after `inject()` when an `Injector` is invoked. `inject()` and `eject()` of the sample `Injector` waits a few seconds and prints a message.

```java
package print;

import com.scalar.kelpie.config.Config;
import com.scalar.kelpie.modules.Injector;
import java.util.Random;

public class PrintInjector extends Injector {
  private Random random;

  public PrintInjector(Config config) {
    super(config);
    this.random = new Random(System.currentTimeMillis());
  }

  @Override
  public void inject() {
    try {
      int waitTime = random.nextInt(5000);
      Thread.sleep(waitTime);
    } catch (InterruptedException e) {
      // ignore
    }

    System.out.println("[Injector] Dummy injection");
  }

  @Override
  public void eject() {
    try {
      int waitTime = random.nextInt(5000);
      Thread.sleep(waitTime);
    } catch (InterruptedException e) {
      // ignore
    }

    System.out.println("[Injector] Dummy ejection");
  }
}
```

You can add multiple `Injector`s. They are invoked by `InjectionExecutor`.

The default executor is `com.scalar.kelpie.executor.RandomInjectionExecutor`. It invokes them one by one. When an `Injector A` starts its `inject()`, any other `Injector` isn't invoked until `Injector A` finishes its `eject()`. And, invoked `Injector` is chosen randomly from all `Injector`s.

In the future, you can choose `InjectionExecutor` by adding `injection_executor` parameter on `[common]` table in the config file.

# Config
You can modify the behavior of your test by the config file. The format of a config file is TOML.

A config consists of at least two tables; `[modules]` and `[common]`. They are used for the execution behavior of Kelpie. And, you can add your own parameters.

## [modules]
Each module is specified with the name and the path of the class file on `[modules]`. For `PreProcessor`, `Processor` and `PostProcessor`, each module is specified with `name` and `path` on each table like `[modules.preprocessor]`. You can specify multiple `Injector`s. `Injector` is specified with the name and the path of the class file like other modules. You have to use an array of the tables `[[modules.injectors]]` to specify `Injector`s.

```toml
[modules]
  [modules.preprocessor]
    name = "org.my.Preparer"
    path = "/path/to/preparer"
  [modules.processor]
    name = "org.my.Processor"
    path = "/path/to/processor"
  [modules.postprocessor]
    name = "org.my.Checker"
    path = "/path/to/checker"
  [[modules.injectors]]
    name = "org.my.Injector1"
    path = "/path/to/injector1"
  [[modules.injectors]]
    name = "org.my.Injector2"
    path = "/path/to/injector2"
```

## [common]
`[common]` is reserved for test behavior. All paramters on this `[common]` table are optional.
You can specify the number of threads which execute `Processor#execute()` by `concurrency`. The default value is 1.
`run_for_sec` can be used for a run time of your test. The default value is 60.
`ramp_for_sec` can be used for the time before the measurement, like a warmup. The default value is 0.
`run_for_sec` and `ramp_for_sec` can be acquired with `Config#getRunForSec()` and `Config#getRampForSec()` in your module.
`injection_executor` is to choose `InjectionExecutor`. The default value is `com.scalar.kelpie.executor.RandomInjectionExecutor`.


```toml
[common]
  concurrency = 4
  run_for_sec = 100
  ramp_for_sec = 10
  injection_executor = "com.scalar.kelpie.executor.RandomInjectionExecutor"
```

## Your parameters
You can set your parameters which are used in your modules by adding tables. In the following example, `[my_test]` and `[my_initial_values]` are defined and 3 prameters are set.

```toml
[my_test]
  test_name = "Test"

[initial_values]
  accounts = 10000
  balance = 1000
```

You can get them by `Config#getUserString(table, name)` and `Config#getUserLong(table, name)`. If you want to get the default value when the specified table or parameter doesn't exists, you use `Config#getUserString(table, name, defaultValue)` or `Config#getUserLong(table, name, defaultValue)`.

```java
  String testName = config.getUserString("my_test", "test_name");
  long numAccounts = config.getUserLong("initial_values", "accounts");
  long initalBalance = config.getUserLong("initial_values", "balance");
  long amount = config.getUserLong("initial_values", "amount", 10);
```
