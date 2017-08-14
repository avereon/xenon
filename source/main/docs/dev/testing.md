# Testing

## Using Monocle
Using Monocole during unit testing will allow the tests to be run in headless
mode. To set up Monocole there are two steps. First is to add the Monocle jar
to the JRE bootstrap libraries. Second is to add the JVM properties to use
Monocle at runtime.

1. Since Monocle jar is needed a bootstrap time, it must be copied to:

    <JDK_HOME>/jre/lib/ext

1. Add the following system properties:

    -Dglass.platform=Monocle -Dmonocle.platform=Headless -Dtestfx.robot=glass -Dprism.order=sw

