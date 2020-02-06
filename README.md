![](https://github.com/scalar-labs/kelpie/workflows/Kelpie/badge.svg)

# Kelpie

Kelpie is a simple yet general framework for running end-to-end testing such as system verification and benchmarking.

A test consists of 4 modules: `PreProcessor`, `Processor`, `PostProcessor` and `Injector`. You can make your own modules.

Before testing, you need to set up your environment.

First, Kelpie executes `PreProcessor#preProcess()` like record population. Next, it executes `Processor#process()`. `Injector#inject()` tries to inject failure or other operations while the `Processor#process()` performs. After `Processor#process()` is completed, Kelpie executes `PostProcessor#postProcess()`.

# Usage
## Build Kelpie
```
./gradlew installDist
```
  - Of course, you can archive Kelpie jar and libraries by `distZip` and so on.

## Build your modules
1. Make your modules: `PreProcessor`, `Processor`, `PostProcessor`, `Injector` in `kelpie/modules`.
    - Refer to sample modules in `kelpie/modules/print`
2. Build them
    ```
    ./gradlew modules
    ```

## Run your test
1. Make a config file
    - Refer to a sample config in `kelpie/modules/print.toml`
    - Specify the path of class files of your modules
    - You can give parameters to your modules by adding your parameters to the config
2. Run a test
    ```
    kelpie/build/install/kelpie/bin/kelpie --config your_config.toml
    ```
    - If `--only-pre` is added, Kelpie will execute only the pre phase. There are other options `--only-process`, `--only-post`.

# Your modules
Kelpie supports your own test with your modules. To use your own modules, you need to implement `PreProcessor`, `Processor` and `PostProcessor`. If needed, `Injector` can be implemented for injection to your test.

## PreProcessor
`PreProcessor` executes something before `Processor`. For example, in a performance benchmark for a database, `PreProcessor` populates the initial records. `PreProcessor#execute()` is always executed on a single thread.

The following class is a sample of `PreProcessor` to just print the title. You have to implement only the constructor and `execute()` method. Parameters of the config file can be acquired from `config` which has been set by the constructor when the class is loaded. Your parameters can be set in the config file like `conf/print.toml`.

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
    int num = config.getUserInteger("print_test", "num");

    for (int i = 0; i < num; i++) {
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
    int num = config.getUserInteger("print_test", "num");

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

You can get them by `Config#getUserString()` and `Config#getUserInteger()`.

```java
  String testName = config.getUserString("my_test", "test_name");
  int numAccounts = config.getUserInteger("initial_values", "accounts");
  int initalBalance = config.getUserInteger("initial_values", "balance");
```
