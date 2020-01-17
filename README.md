![](https://github.com/scalar-labs/kelpie/workflows/Kelpie/badge.svg)

# Kelpie

Kelpie is a simple yet general framework for running end-to-end testing such as system verification and benchmarking.

A test is consist of 4 modules: `PreProcessor`, `Processor`, `PostProcessor` and `Injector`.
You can make your own modules.

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
