# JMeter Live Changes Config

## Overview
This plugin allows users to manipulate (through a REST API) JMeter test plans *on-the-go*.
Here's a couple things that are modifiable:

* Throughput
* Number of active threads
* Variables
* Properties

Users can also:

* View a summary of the test results
* End the test remotely
* Retrieve a quick status of the test (time running, active threads, etc.)

## Why
There are some scenarios where this kind of tool becomes very handy. 
For example;

* Testing the impact of different load scenarios against a system
* Container sizing (on Kubernetes/OSE, for example)
* Correcting certain test parameters while the test is running


## Setup
**Java version 9+ is required**

1. Download the latest JAR from the release section
2. Move the file to your `$JMETER_HOME/lib/ext` folder
3. Start JMeter, load your test plan
4. Add the `Live Changes Config` element to the root of your test plan
5. Set the port you want to be able to communicate with (defaults to `7566`)
6. Start your test
7. By using any HTTP client (Postman, Insomnia, cURL, etc.) communicate with the REST API to change your test's values

## Documentation
An API documentation [is available here](https://anthonygauthier.github.io/jmeter-live-changes-config/), it is directly generated from the [swagger.yaml](docs/swagger.yaml).

---

## Contributing
This plugin is licensed under the MIT license. 

All contributions are welcomed and appreciated.
