## Version 0.0.2

* Changed web server to Jetty + JAX-RS (from webbit-server + webbit-rest)
* Modified `/api/threads/` endpoint to retrieve all thread groups and their properties
* Added `GET /api/threads/{name}` endpoint to retrieve information about a specific thread group
* Added `POST /api/threads/{name}` endpoint to modify a specific thread group
* Added error handling for `GET & POST /threads/{name}` returns RESTful error if thread group doesn't exist
* Added `GET /api/test/status` endpoint to retrieve information about the test status (time running, finished, started, etc.)
* Added `GET /api/test/end` endpoint to end the test run
* Added JavaDoc compatible comments to all the classes/methods


Note: All endpoints require the test to be running to be consumed.

## Version 0.0.1

* Temporary API documentation in README
* Exposes certain routes to access thread group values
* Only works with one thread group
* `GET & POST /variables` to modify variables on the fly
* `GET & POST /properties` to modify properties on the fly
  