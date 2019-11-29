# Command Line
Beyond the normal command line parameters documented in the user manual, 
${project.name} support some command line parameters specifically for 
development activities.

    --execmode=<mode> The execution mode internally changes the settings folder
    so that development, testing and normal execution do not interfere with
    each other. Valid values are devl and test.

# Java VM Arguments
### Locale
Usually the Java virtual machine will use the locale information from the 
operating system. On occasion it may be necessary to override these settings.
To specify the locale for the Java VM use the following JVM parameters:

    -Duser.language=<language>
    -Duser.country=<country>
    -Duser.variant=<variant>

## Examples
To change the language to Spanish

    -Duser.language=es
