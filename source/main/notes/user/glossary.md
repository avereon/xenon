# Glossary

* mod - A mod (short for modification, or module if you prefer) is package that 
provides the program with specific functionality. Mods are used to provide
logic, tools, resource types and other items to the program. Mods, and their
updates, are managed by the program so the user can keep up-to-date with any
mod changes.

* resource - A resource represents something that can be opened or connected to.
Local files are the most common type resources. Other resources are connections 
to servers, databases or even files on other computers. Resources are defined
by a URI. Using the URI and/or the content of the resource the program 
determines what type of resource it is and opens a tool to handle it.

* resource type - A resource type represents common types of computer resources
that can be associated to tools in the program. Resource types are determined
in several ways based on the URI, resource content and/or the 
[media type](https://developer.mozilla.org/en-US/docs/Glossary/MIME_type). All
resources are associated to a resource type in the program. New resource types
and tools are typically registered by mods.

* tool - A tool is a graphical user interface component to view or edit a 
resource. Tools are associated to a resource type and are created when opening a 
resource. The tool stays in the workarea until closed. New tools and resource
types are typically registered by mods.

* workarea - A workarea is a collection of tools and layout in a workspace. It 
is much simpler to switch between workareas than between workspaces. 
Furthermore, workareas can be added, named, renamed and removed easily. Using 
workareas the user can setup as many tool collections as needed.

* workspace - A workspace is a program window and everything contained in it. 
Xenon supports multiple workspaces which can be convenient in multiple monitor
environments. It is less convenient in single monitor

