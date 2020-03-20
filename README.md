![](https://github.com/scalar-labs/kelpie/workflows/Kelpie/badge.svg)

# Kelpie

Kelpie is a simple yet general framework for running end-to-end testing such as system verification and benchmarking.

## How Kelpie works
Kelpie is composed of a framework that orchestrates a test and takes care of task management such as concurrent execution, and a test that is run by the framework.
As the following diagram shows, a test in Kelpie has 3 steps; pre-process, process and post-process, which run in a sequential order. A test can also have an injection step that runs in parallel with the processing step. The behavior of each step can be described by implementing the corresponding modules called `PreProcessor`, `Processor`, `PostProcessor` and `Injector` respectively.

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
`Processor` executes actual tests. For example, if it is benchmarking a database server, `Processor` makes a query, send it to the server, get the response back from the server, and continue them until it finishes.

Like `Preprocessor`, you need to implement the constructor and `execute()`. `execute()` can be executed concurrently with multiple threads if `concurrency` is set to more than 1 in the configuration file. Note that you need to make `execute()` thread-safe in that case.

The following class is an example of `Processor`, which prints out a message for the specified number of times by `num`. 

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
`PostProcessor` executes some tasks after all `Processor#execute()` finish. For example, if it is verifying database consistency, `PostProcessor` reads all the records of the database and checks if their values are as expected. `PostProcessor#execute()` is always executed with a single thread.

Like `PreProcessor` and `Processor`, you need to implement the constructor and `execute()`. The following class is an example of `PostProcessor`, which prints out the specified configurations.

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

    // always succeed in this case

    // if unexpected result happens,
    // throw new PostProcessException("unexpected result happens");
  }
}
```

When `PostProcessor` check the result of `Processor` execution and the result isn't expected, `PostProcessor#execute()` should throw an exception `PostProcessException`. A test will fail when the exception is thrown.

## Injector
`Injector` executes some arbitrary tasks that you want to execute while `Processor#execute()` is running. For example, if it is verifying database consistency in a catastrophic environment, `Injector` kills and restarts a database process randomly and frequently.

You can enable `Injector` by adding `--inject` option to the `kelpie` command.

```console
$ ./kelpie --config my_config.toml --inject
```

The following class is an example of `Injector` that only prints out some text after some random sleep. `Injector` has two methods to implement; `inject()` and `eject()`. Note that `inject` is always executed before `eject()`.

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

When multiple `Injector`s are specified, the way they run concurrently is controlled by `InjectionExecutor`. The currently default `InjectionExecutor` is called `RandomInjectionExecutor`. `RandomInjectionExecutor` randomly selects and invokes one `injector` from the multiple `injector`s at a time.

In the future, you may be able to specify `InjectionExecutor` in the configuration.

# Config
A config is a TOML-formatted file where you can define what modules and static variables to use in your test.
A config file consists of at least a table `[modules]`. `[commmon]` is optional, but it is useful for your test.

## [modules]
`[modules]` is where you specify what modules to run.
As the following example shows, each module needs to be specified with the binary name and the path of a jar file in a respective table such as `[modules.preprocessor]`, `[modules.processor]`, and `[modules.postprocessor]`. Note that an `injector` needs to be specified in an array of tables since we can specify multiple `injector`s. The example uses the same fat jar file for all modules. But you can specify a different jar file for each module.

```toml
[modules]
  [modules.preprocessor]
    name = "print.PrintPre"
    path = "print-modules/build/libs/print-modules-all.jar"
  [modules.processor]
    name = "print.PrintProcessor"
    path = "print-modules/build/libs/print-modules-all.jar"
  [modules.postprocessor]
    name = "print.PrintPost"
    path = "print-modules/build/libs/print-modules-all.jar"
  [[modules.injectors]]
    name = "print.PrintInjector"
    path = "print-modules/build/libs/print-modules-all.jar"
```

## [common]
`[common]` is for reserved static variables that the framework use to change its behavior. All variables on `[common]` table are optional.
`concurrency` is the number of threads to execute in `Processor#execute()`. The default value is 1.
`run_for_sec` is the run time of your test. The default value is 60. This value can be retrieved with `Config#getRunForSec()` from your module.
`ramp_for_sec` is the ramp up time before measurement that can be used for warming up your target system . The default value is 0.  This value can be retrieved with `Config#getRampForSec()` from your module.
`injection_executor` is where you can specify `InjectionExecutor`. The default value is `com.scalar.kelpie.executor.RandomInjectionExecutor`.


```toml
[common]
  concurrency = 4
  run_for_sec = 100
  ramp_for_sec = 10
  injection_executor = "com.scalar.kelpie.executor.RandomInjectionExecutor"
```

## Static variables in your test
You can define static variables that can be used in your modules by defining arbitrary named tables. In the following example,  3 static variables are defined in `[my_test]` and `[my_initial_values]` tables.

```toml
[my_test]
  test_name = "Test"

[initial_values]
  accounts = 10000
  balance = 1000
```

You can get static variables with `Config#getUserString(table, name)` and `Config#getUserLong(table, name)`. If you want to get a default value when a specified table or a static variable doesn't exists, you use `Config#getUserString(table, name, defaultValue)` and `Config#getUserLong(table, name, defaultValue)`.

```java
  String testName = config.getUserString("my_test", "test_name");
  long numAccounts = config.getUserLong("initial_values", "accounts");
  long initalBalance = config.getUserLong("initial_values", "balance");
  long amount = config.getUserLong("initial_values", "amount", 10);
```
