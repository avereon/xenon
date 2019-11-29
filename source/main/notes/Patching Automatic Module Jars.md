# Patching Automatic Module Jars

To generate the module-info.java:

    j11 && /home/ecco/Programs/java/current/bin/jdeps --module-path ~/Downloads/javafx-sdk-11/lib --generate-module-info . reactfx.jar

To compile the module-info.java file to a class:

    j11 && javac -p ~/Downloads/javafx-sdk-11/lib --patch-module reactfx=reactfx.jar reactfx/module-info.java

To update the jar with the module-info.class file:

    j11 && jar uf reactfx.jar -C reactfx module-info.class

