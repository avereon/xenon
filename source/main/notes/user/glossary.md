# Glossary

* mod - A mod (short for modification, or module if you prefer) is package that 
provides the program with specific functionality. Mods are used to provide
logic, tools, asset types and other items to the program. Mods, and their
updates, are managed by the program so the user can keep up-to-date with any
mod changes.

* asset - A asset represents something that can be opened or connected to.
Local files are the most common type assets. Other assets are connections 
to servers, databases or even files on other computers. Assets are defined
by a URI. Using the URI and/or the content of the asset the program 
determines what type of asset it is and opens a tool to handle it.

* asset type - A asset type represents common types of computer assets
that can be associated to tools in the program. Asset types are determined
in several ways based on the URI, asset content and/or the 
[media type](https://developer.mozilla.org/en-US/docs/Glossary/MIME_type). All
assets are associated to a asset type in the program. New asset types
and tools are typically registered by mods.

* tool - A tool is a graphical user interface component to view or edit a 
asset. Tools are associated to a asset type and are created when opening a 
asset. The tool stays in the workarea until closed. New tools and asset
types are typically registered by mods.

* workarea - A workarea is a collection of tools and layout in a workspace. It 
is much simpler to switch between workareas than between workspaces. 
Furthermore, workareas can be added, named, renamed and removed easily. Using 
workareas the user can setup as many tool collections as needed.

* workspace - A workspace is a program window and everything contained in it. 
Xenon supports multiple workspaces which can be convenient in multiple monitor
environments. It is less convenient in single monitor

