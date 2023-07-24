[build-status]: https://github.com/avereon/xenon/workflows/Avereon%20Xenon%20Continuous/badge.svg "Build status"

# Xenon [![][build-status]](https://github.com/avereon/xenon/actions)

Xenon is a basic program framework that provides services common to modern
desktop applications on which specific functionality may be implemented.

Releases are available at https://avereon.com.

## Features

The framework provides the following features:

* **Workspaces** - Xenon provides features for managing the work space. Multiple
  work spaces are allowed and work spaces can be split into various views which
  contain multiple tools.
* **Internationalization** - Xenon supports internationalization and localization
  through the use of resource bundles and locales. Mod developers are encouraged
  to use good internationalization and localization practices.
* **Assets** - Xenon provides features to manage assets (e.g. resources, files,
  connections). In general, assets are defined by URI and then Xenon takes care
  of managing the asset life cycle from beginning to end.
* **Tools** - Xenon provides the base classes for creating tools. Tools are used to
  view and modify assets. Even Xenon uses tools for the program assets like the
  about page and settings.
* **Settings** - Xenon provides features for managing program, mod, tool and asset
  settings. There are already several predefined setting types, and custom types
  can be created.
* **Mods** - Xenon provides features for managing mods (a.k.a. plugins, extends,
  modules, etc.). Xenon is nothing more than a framework and mods provide the
  desired functionality. Mod developers can also set up mod repositories to host
  their mods for their users.
* **Updates** - Xenon provides functionality for handling updates when available.
  Xenon looks for both framework and mod updates from one or more repositories.
  Settings can be changed to manage when and how to apply updates.
* **Notices** - Xenon provides features for managing notices (a.k.a notifications).
  Notices are the most common, and least intrusive, way to interact with the
  user. Notices provide a common API and user interface as well as ways to
  enhance the notice display and user interaction.
* **Themes** - Xenon provides features for managing program themes. Themes are
  easily created by developers and easily selected by users in the settings.
* **Icons** - Xenon provides many predefined icons and provides developers with APIs
  for creating and registering high-quality vector graphics icons. Custom icons
  can be registered and used from mods.
* **Actions** - Xenon provides many predefined actions for use in menus and
  toolbars. Custom actions can be registered and used from mods.
* **Events** - Xenon provides functionality for application events. This includes
  separate event buses for the program, workspace and tool. Listeners can be
  registered for any desired events. Custom events can be defined and used by
  mods and tools.
* **Tasks and Executors** - Xenon provides functionality for program tasks and two
  separate executors (execution queues) for fast running tasks and slow running
  tasks. All internal program tasks use these executors as well.
* **Search and Index** - Xenon provides common search and index functionality. 
  Mod developers are encouraged to provide indexed content for users to find 
  what they need quickly. Settings and actions are indexed by default. 

### Product Roadmap

#### Version 2.0

| Release                                              | Feature Goals                                                                 |      Status |
|------------------------------------------------------|-------------------------------------------------------------------------------|------------:|
| [1.8](https://github.com/avereon/xenon/milestone/10) |                                                                               |    Planning |
| [1.7](https://github.com/avereon/xenon/milestone/9)  | General improvements and bug fixes                                            | In Progress |
| [1.6](https://github.com/avereon/xenon/milestone/8)  | Java 17, Java FX 17, [Acorn Mod](https://github.com/avereon/acorn), bug fixes | 01 Jan 2023 |
| [1.5](https://github.com/avereon/xenon/milestone/7)  | Two-step save, asset reload, bug fixes                                        | 09 Oct 2021 |
| [1.4](https://github.com/avereon/xenon/milestone/6)  | SVG Icons, [Cartesia Mod](https://github.com/avereon/carta)                   | 28 May 2021 |
| [1.3](https://github.com/avereon/xenon/milestone/5)  | Java 14, Java FX 14, flat icons, jpackage installers                          | 03 Jul 2020 |
| [1.2](https://github.com/avereon/xenon/milestone/4)  | Theme modification, more dark and light themes                                | 03 May 2020 |
| [1.1](https://github.com/avereon/xenon/milestone/3)  | Example Mod (Mazer)                                                           | 22 Feb 2020 |

#### Version 1.0

| Release                                             | Feature Goals                                               |      Status |
|-----------------------------------------------------|-------------------------------------------------------------|------------:|
| [1.0](https://github.com/avereon/xenon/milestone/2) | Documentation, licensing, housekeeping                      | 28 Nov 2019 |
| [0.9](https://github.com/avereon/xenon/milestone/1) | Custom tool pane and tabs, CI configuration                 | 19 Sep 2019 |
| 0.8                                                 | Mod manager, mod tool, mod updates                          | 16 Jun 2019 |
| 0.7                                                 | Java 11, Java FX 11, status bar, memory status, task status | 24 Jan 2019 |
| 0.6                                                 | Update manager, product updates                             | 04 Jun 2018 |
| 0.5                                                 | Program settings, settings tool                             | 22 Oct 2017 |
| 0.4                                                 | Welcome tool, guide tool, about tool                        | 21 Sep 2017 |
| 0.3                                                 | Asset manager, product assets                               | 05 Aug 2017 |
| 0.2                                                 | Workarea, menu bar, tool bar                                | 30 May 2017 |
| 0.1                                                 | Initial UI, settings manager                                | 07 Apr 2017 |

### Contact Information

* This project is owned by Avereon
* Contact [support@avereon.com](mailto:support@avereon.com) for information

### Dependency Licenses

* OpenJDK - [GNU General Public License, version 2, with the Classpath Exception](https://openjdk.java.net/legal/gplv2+ce.html)
* OpenJFX - [GNU General Public License, version 2, with the Classpath Exception](https://openjdk.java.net/legal/gplv2+ce.html)
* Avereon Zevra (Utility Library - Non-UX) - [MIT](https://avereon.com/license/mit/)
* Avereon Zarra (Utility Library - UX) - [MIT](https://avereon.com/license/mit/)
* Avereon Zenna (Icon Library) - [MIT](https://avereon.com/license/mit/)
* Avereon Weave (Updater) - [MIT](https://avereon.com/license/mit/)
