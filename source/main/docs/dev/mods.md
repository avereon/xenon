# Mods

Extension modules (or plugins) are called mods in ${project.name}. In their simplest form
mods are nothing more than an implementation of the Mod interface. However,
when packaged and made available from a market site Mods are a powerful feature
of ${project.name}.

## Setup

The easiest way to start a Mod project is to use the 
com.xeomar.xenon.mod parent pom. The parent pom will ensure that the module is 
packaged properly for ${project.name}:

~~~~
<parent>
	<groupId>com.xeomar.pom</groupId>
	<artifactId>mod</artifactId>
	<version>3.0.0-SNAPSHOT</version>
</parent>
~~~~

The project structure is typical for a 
Maven Java project and the simplest module requires only three files: the Java
file for the Mod, the Java module-info.java file and the mod 
[product card](./product-card.md) file.

~~~~
src/main/java/<package>/<Mod>.java
src/main/java/module-info.java
src/main/resources/META-INF/product.card
~~~~

The Java module-info.java file must have a 'provides' clause for the Mod class. 
You should replace com.example.sample.SampleMod with your fully qualified class
name:

~~~~
provides Mod with com.example.sample.SampleMod
~~~~

## Implementation

The implementing class must extend com.xeomar.xenon.Mod. It must also have
a no-args constructor and implement the register(), startup(), shutdown() and 
unregister() methods.

register() - Does there need to be register and unregister?

startup()

shutdown()

unregister() - Does there need to be register and unregister?

