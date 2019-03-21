# JMeter Live Changes Config

## Overview
This plugin, which is still in an early development stage __(v 0.0.2)__, allows the user to communicate with a RESTful API to change JMeter tests to change values such as:

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
- [x] Support for multiple `Thread Group`
- [ ] Support for native distributed load testing
- [ ] Support for distributed load testing through multiple injectors executing the same test
- [x] Initial documentation of the REST API
- [x] Restructure code-base so it's easier to read and maintain
- [ ] Generate a Swagger (or some sort of API docs)
- [ ] Publish version __1.0.0__ to [JMeter-Plugins](https://jmeter-plugins.org/)

## Known issues/limitations

As of right now, I am aware of a few limitations;

### Issues
* Everything that's not checked on the roadmap is not working.
* The `GET /api/threads AND /api/threads/{name}` endpoint seem to be returning 0 active threads (will be fixed in 0.0.3)

### Limitations
* The `Live Changes Config` __must__ be declared at the root of your `Test Plan`
## Setup

1. Download the latest JAR from the release section
2. Move the file to your `JMETER_HOME/lib/ext` folder
3. Start JMeter, load your test plan
4. Add the `Live Changes Config` element to the root of your test plan
5. Set the port you want to be able to communicate with (defaults to `7566`)
6. Start your test
7. By using any HTTP client (Postman, Insomnia, cURL, etc.) communicate with the REST API to change your test's values

** The API documentation is still under construction **

## Temporary API Doc

`GET /api/variables`: Retrieves the variables declared for your test

---

`POST /api/variables`: Send (as JSON) your variables changes.

__Request__ example:
```json
{
  "myInt": 1,
  "myString": "foo"
}
```

---

`GET /api/threads`: Retrieves information about all thread groups

---
`GET /api/threads/{name}`: Retrieves information about a specific thread group

---

`POST /api/threads/{name}`: Send data (as JSON) to modify a specific thread group values.

__Request__ example:
```json
{
  "threadNum": 5,
  "delay": 0
  // etc.
}
```

---

`GET /api/properties`: Retrieves the properties declared for your test

---

`POST /api/properties`: Send (as JSON) your properties changes.

__Request__ example:
```json
{
  "myProp": "bar"
}
```

---

`GET /api/test/status`: Retrieves basic info about the test

__Response__ example :
```json
{
    "startTime": 0,
    "runningTime": 0,
    "totalActiveThreads": 0,
    "totalThreads": 0
}
``` 

---
`GET /api/test/end`: Ends the test run

__Response__ example :
```json
{
    "info": "success",
    "description": "Threads will gracefully stop."
}
``` 

---
`GET /api/test/summary`: Retrieves a summary of the test results for every sampler

__Response__ example :
```json
[
    {
        "testSample": {
            "averageLatency": 75,
            "averageResponseTime": 797,
            "minResponseTime": 0,
            "maxResponseTime": 417,
            "hitsPerSecond": 3,
            "sentBytesPerSecond": 0,
            "totalErrors": 0,
            "errorPercentage": 0,
            "receivedBytesPerSecond": 352,
            "timeRunning": 4055,
            "90thPercentile": 349,
            "totalSamples": 12,
            "totalBytes": 1368,
            "averageBytes": 114,
            "totalSentBytes": 0,
            "standardDeviation": -554
        }
    },
    {
        "testSample - 2": {
            "averageLatency": 98,
            "averageResponseTime": 955,
            "minResponseTime": 0,
            "maxResponseTime": 450,
            "hitsPerSecond": 3,
            "sentBytesPerSecond": 0,
            "totalErrors": 0,
            "errorPercentage": 0,
            "receivedBytesPerSecond": 369,
            "timeRunning": 4055,
            "90thPercentile": 393,
            "totalSamples": 13,
            "totalBytes": 1482,
            "averageBytes": 114,
            "totalSentBytes": 0,
            "standardDeviation": -708
        }
    }
]
``` 

---




## Contributing
This plugin is licensed under the MIT license. 

All contributions are welcomed and appreciated.

#### Supporting the author
[![patreon](https://c5.patreon.com/external/logo/become_a_patron_button.png)](https://www.patreon.com/bePatron?u=17797269)

