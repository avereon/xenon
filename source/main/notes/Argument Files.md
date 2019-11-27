# Argument Files
Java has a new feature to add arguments to the JVM by specifying one or more 
argument files. Multiple files may be specified and parameters are added in the 
order they are specified. Unfortunately that does not work so well when used to 
have a default set of arguments and an override set of arguments.

Example:

    /home/user/Programs/xenon/bin/java @/home/user/Programs/xenon/bin/default.args @/home/user/.config/xenon/program.args
    
Sample argument file:

    -Xmn128m
    -Xmx256m
    -m com.avereon.xenon/com.avereon.xenon.Program
