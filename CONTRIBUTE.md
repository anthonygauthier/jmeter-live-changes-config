### Things to know to contribute to the codebase

  - To be able to change values while the test is running, we make use of the `LiveChanges.iterationStart()`. 
  This acts as an "event loop" and allows us to create methods such as `checkForThreadChanges()`, `checkForVariableChanges()`, etc.
  - We use static variables to be able to modify values from outside `JMeterContext` and pass them to the event loop.
