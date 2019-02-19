# JMeter Live Changes Config

## Overview
This plugin, which is still in an early development stage __(v 0.0.1)__, allows the user to communicate with a RESTful API to change JMeter tests to change values such as:

* Throughput
* Number of active threads
* Variables
* Properties

## Why
Here are some scenarios where this kind of tool becomes very handy:

* Testing the impact of different load scenarios against a system
* Container sizing (Kubernetes/OSE, for example)
* Correcting certain test parameters while the test is running

... And many more.

## Roadmap
- [x] Add threads
- [x] Remove threads
- [ ] Get variables
- [ ] Modify variables
- [ ] Get properties
- [ ] Modify properties
- [ ] Support for native distributed load testing
- [ ] Support for distributed load testing through multiple injectors executing the same test

## Contributing
This plugin is licensed under the MIT license. 

All contributions are welcomed and appreciated.