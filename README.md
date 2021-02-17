# Yet another Java OBD2 client library

## About

This is yet another java framework that is intended to simplify communication with OBD2 adapters like ELM327 clones.
The goal of the framework is to provide set of useful features that allows to collect and process vehicle metrics.
Example usage can be found under: [Android OBD2 data logger](https://github.com/tzebrowski/AlfaDataLogger "AlfaDataLogger") 


## What makes this framework unique ?

### Pid definitions

* Framework uses external JSON files that defines series of supported PID's (SAE J1979) and evaluations formula. Default configuration has following structure 

```json
{
 "mode": "01",
 "pid": 23,
 "length": 2,
 "description": "Fuel Rail Pressure (diesel)",
 "min": 0,
 "max": 655350,
 "units": "kPa (gauge)",
 "formula": "((A*256)+B) * 10"
},
```


* Framework is able to work with multiple sources of PID's that are specified for different automotive manufacturers.
* Generic list of PIDs can be found [here](./src/main/resources/mode01.json "mode01.json")



### Batch commands

Framework allows to ask for up to 6 PID's in a single request.

*Request:*

``` 
01 01 03 04 05 06 07
```

*Response:*

``` 
0110:4101000771611:0300000400051c2:06800781000000
```


### Multiple decoders for the single PID

You can add multiple decoders for single PID. In the example bellow there are 2 decoders for PID 0115. 
One that calculates AFR, and second one shows Oxygen sensor voltage.

```json
{
  "id": "22",
  "mode": "01",
  "pid": 15,
  "length": 2,
  "description": "Calculated AFR",
  "min": "0",
  "max": "20",
  "units": "Volts %",
  "formula": "parseFloat( ((0.680413+((0.00488*(A / 200))*0.201356))*14.7).toFixed(2) )"
},
{
  "id": "23",
  "mode": "01",
  "pid": 15,
  "length": 2,
  "description": "Oxygen sensor voltage",
  "min": "0",
  "max": "5",
  "units": "Volts %",
  "formula": "parseFloat(A / 200)"
}

```

### Fully mockable interfaces

There is no required to have device to play with the framework. 
Connection to the device can be mocked using `MockConnection` class.

Usage in E2E tests

```java
final Workflow workflow = WorkflowFactory.generic()
				.equationEngine("JavaScript")
				.ecuSpecific(EcuSpecific
					.builder()
					.initSequence(AlfaMed17CommandGroup.CAN_INIT_NO_DELAY)
					.pidFile("alfa.json").build())
				.observer(new DummyObserver())
				.build();
		
final Set<Long> ids = new HashSet<>();
ids.add(8l); // Coolant
ids.add(4l); // RPM
ids.add(7l); // Intake temp
ids.add(15l);// Oil temp
ids.add(3l); // Spark Advance

final MockConnection connection = MockConnection.builder()
				.commandReply("221003", "62100340")
				.commandReply("221000", "6210000BEA")
				.commandReply("221935", "62193540")
				.commandReply("22194f", "62194f2d85")
				.commandReply("221812", "")
				.build();

workflow.connection(connection).filter(ids).batch(false).start();
final Callable<String> end = () -> {
	Thread.sleep(1 * 1500);
	log.info("Ending the process of collecting the data");
	workflow.stop();
	return "end";
};


double ratePerSec05 = workflow.getStatistics().getRatePerSec(engineTemp);
double ratePerSec0C = workflow.getStatistics().getRatePerSec(pids.findBy(12l));

Assertions.assertThat(ratePerSec05).isGreaterThan(10d);
Assertions.assertThat(ratePerSec0C).isGreaterThan(10d);

```


### Support for 22 mode

* It has support for mode 22 PIDS
* Configuration: [alfa.json](./src/main/resources/alfa.json?raw=true "alfa.json")
* Integration test: [AlfaIntegrationTest](./src/test/java/org/obd/metrics/integration/AlfaIntegrationTest.java "AlfaIntegrationTest.java") 


### Formula calculation

* Framework is able to calculate equation defined within Pid's definition to get PID value. 
It may include additional JavaScript functions like *Math.floor* ..

``` 
Math.floor(((A*256)+B)/32768((C*256)+D)/8192)
```



## Supported devices

* ELM 1.5
* ELM 2.2


## Verified against 

So far FW has been verified against following ECU
* MED 17.3.1
* MED 17.5.5
* EDC 15.x


## Design view

###  Component view


![Alt text](./src/main/resources/component.png?raw=true "Component view")


###  Model view


![Alt text](./src/main/resources/model.png?raw=true "Model view")


###  API

API of of FW is exposed through the [Workflow](./src/main/java/org/obd/metrics/api/Workflow.java "Workflow.java") interface and its implementations.
Implementations of particular workflow can be instantiated by [WorkflowFactory](./src/main/java/org/obd/metrics/api/WorkflowFactory.java "WorkflowFactory.java")

```java

public interface Workflow {

void start();

void stop();

PidRegistry getPids();

StatisticsAccumulator getStatistics();

Workflow connection(Connection connection);

Workflow filter(Set<Long> filter);

Workflow batch(boolean batchEnabled);
}

```

# Architecture drivers

1. Extensionality
2. Reliability


