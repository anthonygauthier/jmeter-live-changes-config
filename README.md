# JMeter Live Changes Config

### Support the author
[![patreon](https://c5.patreon.com/external/logo/become_a_patron_button.png)](https://www.patreon.com/bePatron?u=17797269)

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
- [x] Modify variables
- [x] Get properties
- [x] Modify properties
- [ ] Support for multiple `Thread Group`
- [ ] Support for native distributed load testing
- [ ] Support for distributed load testing through multiple injectors executing the same test
- [ ] Initial documentation of the REST API
- [x] Restructure code-base so it's easier to read and maintain
- [ ] Publish version __1.0.0__ to [JMeter-Plugins](https://jmeter-plugins.org/)

## Known issues/limitations

As of right now, I am aware of a few limitations;

* Of course, everything that's not checked on the roadmap is not working.
* The live changes plugin currently only works with one Thread Group. There seems to be issues when more than one thread groups are involved in a test plan.

## Setup

1. Download the latest JAR from the release section
2. Move the file to your `JMETER_HOME/lib/ext` folder
3. Start JMeter, load your test plan
4. Add the `Live Changes Config` element to your Thread Group
5. Set the port you want to be able to communicate with (defaults to `7566`)
6. Start your test
7. By using any HTTP client (Postman, Insomnia, cURL, etc.) communicate with the REST API to change your test's values

** The API documentation is still under construction **

## Temporary API Doc

`GET /variables`: Retrieves the variables declared for your test

---

`POST /variables`: Send (as JSON) your variables changes.

Example:
```json
{
  "myInt": 1,
  "myString": "foo"
}
```

---

`GET /threads`: Retrieves the number of active threads.

---

`POST /threads`: Send (as JSON) your new active threads number.

Example:
```json
{
  "threadNum": 5
}
```

---

`GET /properties`: Retrieves the properties declared for your test

---

`POST /properties`: Send (as JSON) your properties changes.

Example:
```json
{
  "myProp": "bar"
}
```

---




## Contributing
This plugin is licensed under the MIT license. 

All contributions are welcomed and appreciated.
