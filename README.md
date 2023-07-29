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
* Container sizing (on Kubernetes/OSE, for example)
* Correcting certain test parameters while the test is running

... And many more.

## Roadmap
- [x] Add threads
- [x] Remove threads
- [x] Get variables
- [ ] Modify variables
- [ ] Get properties
- [ ] Modify properties
- [ ] Support for native distributed load testing
- [ ] Support for distributed load testing through multiple injectors executing the same test
- [ ] Document REST API
- [ ] Restructure code-base so it's easier to read and maintain

## Known issues/limitations

As of right now, I am aware of a few limitations;

* Of course, everything that's not checked on the roadmap is not working.
* The live changes plugin currently only works with one Thread Group. There seems to be issues when more than one thread groups are involved in a test plan.

## Contributing
This plugin is licensed under the MIT license. 

All contributions are welcomed and appreciated.