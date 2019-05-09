# Argument Files
Java has a new feature to add arguments to the JVM by specifying
one or more argument files. Multiple files may be specified and
it appears that the last one for a particular argument wins.

Example:

    /home/user/Programs/xenon/bin/java @/home/user/Programs/xenon/bin/default.args @/home/user/.config/xenon/program.args
    
Sample argument file:

    -Xmn128m
    -Xmx256m
    -m com.xeomar.xenon/com.xeomar.xenon.Program
