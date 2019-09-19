# Command Line Interface
###  Starting Xenon
Xenon is a Java module and as such should be started using the Java 
module command line parameters. Xenon has also been distributed as a 
jlinked application and should be executed using the provided Java runtime. To 
start Xenon use the following commands:

    $(xenon.home)/bin/java -m com.avereon.xenon

It is expected, however, that users use the provided program shortcuts and shell 
scripts such that Xenon may simply be started by using the command 
`xenon` on the command line from any folder. The remainder of 
this document will assume that Xenon is available to execute in this 
manner:

    xenon [<option>...] [<url>...]

## Parameters
Xenon supports named option and unnamed url command line parameters. 

### URLs
URL parameters indicate the resources that should be opened by Xenon.
Any valid URL should be acceptable but may not be able to be supported by 
Xenon if a supporting mod has not been installed. Also, special 
handling of file URLs is supported since most URL parameters are based on the 
local file system. Any URL that does not specify a scheme will be interpreted
as a file URL. Relative URLs will be resolved against user.path system property
of the invoking JVM.

Examples:

    Open the readme.txt located in the user path:
    xenon.jar readme.txt
    
    Open the Linux message of the day file:
    xenon.jar /etc/motd
    
    Open the Xenon web site
    xenon.jar https://avereon.com/products/xenon/

### Options
Xenon supports several simple and valued options. Options always begin 
with '--' followed by the option name (e.g --help). Options that use values have 
an '=' following the name and before the value (e.g --log-level=info).

####
Program Options

    --status - Show the program status (stopped, running)
    --version - Show the program version and exit
    --stop - Stop the currently running instance (without prompting)

####
Logging Options

    --log-level=<level> The logging level for the program. Valid values are
      none, error, warn, info, debug, trace and all. The default is info.
      
      none - Turn off all logging
      error - Events that may leave the program in an unstable state
      warn - Events that usually leave the program in a stable state
      info - Information messages like normal program events
      debug - Extra messages that show high level events
      trace - Extra messages that show low level events
      all - Turn on all logging

