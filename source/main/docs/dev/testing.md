# Testing

## Using Monocle with Maven
Using Monocle during unit testing will allow the tests to be run in headless
mode. All the dependencies and properties have been done in the pom so you simply
just run maven with the headless profile enabled.

        mvn clean package -Pheadless

