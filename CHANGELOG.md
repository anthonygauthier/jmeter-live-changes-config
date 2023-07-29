## Version 0.0.3

* Fixed #7
    * `GET /api/threads` now correctly returns the thread groups' information
* Fixed #15
    * TravisCI was giving an error since the `openjdk8` agent is no longer available. Now using `openjdk13` as it's the java version used to develop the plugin.
* Fixed #10
    * Completely reworked the `GET /api/test/summary` endpoint, most computations now use the `Calculator` class from the JMeter core.
* The `*.properties` files are now correctly included in the `pom.xml`, text is now correctly shown in JMeter
* Fixed #13 and #14
    * Side-fixes of the whole refactoring that was done in 0.0.3

## Version 0.0.2

* Changed web server to Jetty + JAX-RS (from webbit-server + webbit-rest)
* Modified `/api/threads/` endpoint to retrieve all thread groups and their properties
* Added `GET /api/threads/{name}` endpoint to retrieve information about a specific thread group
* Added `POST /api/threads/{name}` endpoint to modify a specific thread group
* Added error handling for `GET & POST /threads/{name}` returns RESTful error if thread group doesn't exist
* Added `GET /api/test/status` endpoint to retrieve information about the test status (time running, finished, started, etc.)
* Added `GET /api/test/end` endpoint to end the test run
* Added JavaDoc compatible comments to all the classes/methods
* Added `ResultHolder` class which calculates and holds a summary of the test results
* Added `GET /api/test/summary` endpoint to retrieve a results summary of every sample (#1)


Note: All endpoints require the test to be running to be consumed.
## Version 0.0.1

* Temporary API documentation in README
* Exposes certain routes to access thread group values
* Only works with one thread group
* `GET & POST /variables` to modify variables on the fly
* `GET & POST /properties` to modify properties on the fly
  