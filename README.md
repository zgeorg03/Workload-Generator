## Workload Generator
A simple java application to generate artificial load for web-based applications.

![image](https://github.com/zgeorg03/Workload-Generator/blob/experimental/docs/architecture.png)
## Features
TODO

## How to run?
The following code snippet builds the source code and packages the application into
an executable file.
```bash
mvn clean package
```
To run the executable file, use the following command:
```bash
java -jar target/workload-generator-1.0.jar
```
Make sure to have a valid *config.yml* in working directory.

## Configuration

The configuration file allows the user to describe and configure the artificial
workload to produce. The following table describes all the available control parameters.

|Key| Decription|
|---|-----------|
|experiment| The name of the running experiment|
|threads| The number of  threads to use for serving HTTP requests.|
|outputTime| The time period (ms) for aggregating statistics and logging information.|
|minOperationsPerSec| The global minimum number of operations per second to execute |
|maxOperationsPerSec| The global maximum number of operations per second to execute |
|timeout| The global timeout period for an HTTP request to be served|
|generators| See [Generators Table](#generators)|
|sequence|  See [Sequence Table](#sequence)|
|operations| See [Operations Table](#Operations) |

### Generators
TODO...

### Sequence
TODO...

### Operations
TODO...



## Changes
1. Replace SnakeYaml with another library.
2. ~~Produce a directory, where each log file represents a specific operation.~~
