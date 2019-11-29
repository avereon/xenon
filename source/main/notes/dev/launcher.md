# Program Launcher
Java does not always fit into the operating system it is running on. This is 
particularly noticeable on the Windows operating system. Most Unix style 
systems have the ability to create application "shortcuts" that work quite 
well with Java applications and normally they do not have any problem working 
with the various desktops and their associated behaviors. But Windows has 
special behaviors that are only possible with a custom executable, not just a 
shortcut.

## Windows Behaviors
There are several Windows behaviors that are only possible with a custom 
executable and not the Java executable, javaw.exe. The following behaviors
require a custom executable:

- Identifying multiple windows are from the same application - A feature of 
Windows 7 and later is the ability for Windows to combine task bar buttons 
from the same application in to one group. This only works if the windows 
are all started from the same executable. It does not appear that javaw.exe
follows this rule since different program started from javaw.exe do not
collapse.

## Java 9
Most Java launchers work with versions prior to Java 9. Java 9 significantly
changes the JRE to the point that any launchers up to this point cannot start
an application on Java 9.  

## Launcher Options

### Launch4j (http://launch4j.sourceforge.net/)
Launch4j is a fairly well supported Java launcher. It has an active user and
development base. It does have some problems supporting some of the Windows 10
behaviors.

### Winrun4j (http://winrun4j.sourceforge.net/)
Winrun4j is a lesser known Java launcher that mostly succeeds as a Windows 10
launcher. The only minor issue is the small inconvenience of having to have
a 32-bit and 64-bit version that correspond with the JVM version the program
will use. This normally works out fine but can cause a problem for those that
have a mixed architecture on their machines. Unfortunately it does not look
like it has an active developer base.
