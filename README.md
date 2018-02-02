# Workload Generator
A workload generator for web-based applications

## Instructions
Make sure you have increase the maximum number of open files, using the following command:
```bash
ulimit -n 4096
```
To build and run simply run the following commands:
```bash
mvn clean && mvn package
java -jar target/WorkloadGen.jar

```

## Configuration File

This json file describes the operations and the type of the workload to be executed. 
### Config

```json
"config":{
	"generator" :{
		"type" : "sine",
			"period" : 300
	},
		"maxThreads" : 64,
		"minOperations" :10 ,
		"maxOperations" : 50,
		"outputTime" : 5
}
```
With the **config** field you specify the type of the generator, the maximum number of threads, the minimum and maximum number of operations per second to be executed, and the logging output time.
Throughput must be a decimal number between 0 and 1. In this example, a throughput of 0, results to the minimum number of operations per second which is 10 and a throughput of 1, results to 50 operations per second.
###  Operations

You should specify in the configuration file at least one operation. The operation consists of an Http request. For example: 
```json
{
	"operationId": "readLight",
		"weight":4,
		"url": "http://10.16.3.38:8093/query/service",
		"method": "POST",
		"data": {
			"statement":"SELECT * FROM `beer-sample` LIMIT 10"
		}
}
```
In the above example we specify a POST request to a CouchBase server. Note that data field is necessary and contains the N1QL query to be executed. We also specify the weight of this operations, used for the random execution of multiple operations.

###  Workload Types
Currently we support 2 types of workloads.

#### Gaussian
Every second the throughput of operations is random number from the Gaussian Distribution.
```json
    "generator" : {
      "type" : "gaussian",
      "mean" : 0.5,
      "deviation" : 0.1

    }
```
In this example the gaussian distribution has a mean of 0.5 and a standard deviation of 0.1

#### Sine
Every second the throughput follows the sine wave with an amplitude between the minimum and maximum operations.
```json
    "generator" :{
      "type" : "sine",
      "period" : 300
    }
```
In this example the sine wave has a 5 minutes period.
#### Mountain
There are 3 phases.
```json
    "generator" :{
      "type" : "mountain",
      "ascent" : 120,
      "plateau" : 300,
      "descent" : 120
    }
```
In this example the ascending phase is 2 minutes in which the throughput increases linearly.
After 2 minutes, throughput remains for 5 minutes around 100% and finally for 2 minutes
it decreases linearly to zero. The process is repeated infinitely.
