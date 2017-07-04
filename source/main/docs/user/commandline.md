# Command Line
The ${project.name} follows the form:

    ${project.artifactId}.jar [<option>...] [url...]

### URL Parameters
URL parameters indicate the resources that should be opened by ${project.name}.
Any valid URL should be acceptable but may not be able to be supported by 
${project.name} if a supporting module has not been installed. Also, special 
handling of file URLs is supported since most URL parameters are based on the 
local file system. Any URL that does not specify a scheme will be interpreted
as a file URL. Relative URLs will be resolved against user.path system property
of the invoking JVM.

Examples:

    Open the readme.txt located in the user path:
    ${project.artifactId}.jar readme.txt
    
    Open the Linux message of the day file:
    ${project.artifactId}.jar /etc/motd
    
    Open the ${project.name} web site
    ${project.artifactId}.jar http://parallelsymmetry.com/software/${project.artifactId}/

### Option Parameters
${project.name} supports named and unnamed command line parameters. Named
parameters start with '--' like --help and parameters that take values have
and '=' in front of the value like --log-level=info.

Logging

    --log-level=<level> The logging level for the product. Valid values are
    none, error, warn, info, debug, trace and all. The default is info.
      
      none - Turn off all logging
      error - Events that may leave the product in an unstable state
      warn - Events that usually leave the product in a stable state
      info - Information messages like normal product events
      debug - Extra messages that show high level events
      trace - Extra messages that show low level events
      all - Turn on all logging

