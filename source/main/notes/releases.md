# Xenon Release Process

A Xenon release is expected to have all the dependencies released as well, including Maven parent poms and Maven plugins. This amounts to nearly a dozen different projects that need to be released,
one by one, leading up to the Xenon release. This document helps coordinate the process:

1. For each dependency project
    1. Update dependency versions to non-SNAPSHOT versions, build and test
    1. Set the project version to a non-SNAPSHOT version, build and test
    1. Commit and push the version changes, this should trigger the release build
    1. Update the project version to the next SNAPSHOT version
    1. Update any dependency versions if desired to the next version
    1. Before committing and pushing, be sure the release build is complete and successful
    1. Commit and push the version changes, this should trigger a new build

## Dependency Release List for Version 1.7

| Project               | Old Version  | New Version |   Status | Next Status |
|-----------------------|--------------|-------------|---------:|-------------|
| Maven Parent POMs     |
| top                   | 3.5.3        | 3.5.3       | Complete |
| jar                   | 3.5.3        | 3.5.3       | Complete |
| asm                   | 3.5.3        | 3.5.3       | Complete |
| prd                   | 3.5.3        | 3.5.3       | Complete |
| prg                   | 3.5.3        | 3.5.3       | Complete |
| mod                   | 3.5.3        | 3.5.4       | Complete |
| Avereon Libraries     |
| zevra                 | 0.9-SNAPSHOT | 0.9         | Complete | Complete    |
| zarra                 | 0.9-SNAPSHOT | 0.9         | Complete | Complete    |
| zenna                 | 0.9-SNAPSHOT | 0.9         | Complete | Complete    |
| curve                 | 0.4-SNAPSHOT | 0.4         | Complete | Complete    |
| marea                 | 0.2-SNAPSHOT | 0.2         | Complete | Complete    |
| Avereon Maven Plugins |
| curex                 | 1.4.1        | 1.4.1       | Complete | Complete    |
| cameo                 | 2.10         | 2.10        | Complete | Complete    |
| Avereon Applications  |
| weave                 | 1.4-SNAPSHOT | 1.4         | Complete | Complete    |
| xenon                 | 1.7-SNAPSHOT | 1.7         | Complete | Complete    |
| Xenon Test Utilities  |
| zerra                 | 0.9-SNAPSHOT | 0.9         | Complete | Complete    |
| Avereon Mods          |
| acorn                 | 1.2.4        | 1.3         |          |
| aveon                 | 1.2-SNAPSHOT | 1.2         |          |
| carta                 | 1.3-SNAPSHOT | 1.3         |          |
| mazer                 | 1.3-SNAPSHOT | 1.3         |          |
| recon                 | 1.2-SNAPSHOT | 1.2         |          |


## Dependency Release List for Version 1.6

| Project               | Old Version  | New Version |   Status |
|-----------------------|--------------|-------------|---------:|
| Maven Parent POMs     |
| top                   | 3.4.0        | 3.4.0       | Complete |
| jar                   | 3.3.3        | 3.3.3       | Complete |
| asm                   | 3.3.1        | 3.3.1       | Complete |
| prd                   | 3.3.3        | 3.3.3       | Complete |
| prg                   | 3.3.3        | 3.3.3       | Complete |
| mod                   | 3.3.3        | 3.3.3       | Complete |
| Avereon Libraries     |
| zevra                 | 0.8-SNAPSHOT | 0.8         | Complete |
| zarra                 | 0.8-SNAPSHOT | 0.8         | Complete |
| zenna                 | 0.8-SNAPSHOT | 0.8         | Complete |
| curve                 | 0.3-SNAPSHOT | 0.3         | Complete |
| marea                 | 0.1-SNAPSHOT | 0.1         | Complete |
| Avereon Maven Plugins |
| curex                 | 1.3          | 1.3         | Complete |
| cameo                 | 2.9          | 2.9         | Complete |
| Avereon Applications  |
| weave                 | 1.3-SNAPSHOT | 1.3         | Complete |
| xenon                 | 1.6-SNAPSHOT | 1.6         | Complete |
| Xenon Test Utilities  |
| zerra                 | 0.8-SNAPSHOT | 0.8         | Complete |
| Avereon Mods          |
| carta                 | 1.2-SNAPSHOT | 1.2         | Complete |


## Dependency Release List for Version 1.5

| Project | Old Version    | New Version |  Status  |
|---------|----------------|-------------|---------:|
| top     | 3.2.0-SNAPSHOT | 3.2.0       | Complete |
| jar     | 3.2.0-SNAPSHOT | 3.2.0       | Complete |
| prd     | 3.2.0-SNAPSHOT | 3.2.0       | Complete |
| prg     | 3.2.0-SNAPSHOT | 3.2.0       | Complete |
| mod     | 3.2.0-SNAPSHOT | 3.2.0       | Complete |
|         |                |             |          |
| zevra   | 0.6-SNAPSHOT   | 0.7         | Complete |
| zarra   | 0.5-SNAPSHOT   | 0.7         | Complete |
| zerra   | 0.5-SNAPSHOT   | 0.7         | Complete |
| zenna   | 0.6-SNAPSHOT   | 0.7         | Complete |
|         |                |             |          |
| curex   | 1.3-SNAPSHOT   | 1.3         | Complete |
| cameo   | 2.8-SNAPSHOT   | 2.9         | Complete |
|         |                |             |          |
| weave   | 0.5-SNAPSHOT   | 1.1         | Complete |
| xenon   | 1.5-SNAPSHOT   | 1.5         | Complete |
|         |                |             |          |
| curve   | 0.2-SNAPSHOT   | 0.2         | Complete |
| carta   | 1.1-SNAPSHOT   | 1.1         | Complete |


## Dependency Release List for Version 1.4

| Project | Old Version    | Rel Version | New Version    |  Status  |
|---------|----------------|-------------|----------------|---------:|
| top     | 3.1.0-SNAPSHOT | 3.1.0       | 3.2.0-SNAPSHOT | Complete |
| jar     | 3.1.0-SNAPSHOT | 3.1.0       | 3.2.0-SNAPSHOT | Complete |
| prd     | 3.1.0-SNAPSHOT | 3.1.0       | 3.2.0-SNAPSHOT | Complete |
| prg     | 3.1.0-SNAPSHOT | 3.1.0       | 3.2.0-SNAPSHOT | Complete |
| mod     | 3.1.0-SNAPSHOT | 3.1.0       | 3.2.0-SNAPSHOT | Complete |
|         |                |             |                |          |
| zevra   | 0.5-SNAPSHOT   | 0.5         | 0.6-SNAPSHOT   | Complete |
| zarra   | 0.4-SNAPSHOT   | 0.4         | 0.5-SNAPSHOT   | Complete |
| zerra   | 0.4-SNAPSHOT   | 0.4         | 0.5-SNAPSHOT   | Complete |
| zenna   | 0.5-SNAPSHOT   | 0.5         | 0.6-SNAPSHOT   | Complete |
|         |                |             |                |          |
| curex   | 1.2-SNAPSHOT   | 1.2         | 1.3-SNAPSHOT   | Complete |
| cameo   | 2.7-SNAPSHOT   | 2.7         | 2.8-SNAPSHOT   | Complete |
|         |                |             |                |          |
| weave   | 0.4-SNAPSHOT   | 0.4         | 0.5-SNAPSHOT   | Complete |
| xenon   | 1.4-SNAPSHOT   | 1.4         | 1.5-SNAPSHOT   | Complete |
|         |                |             |                |          |
| curve   | 0.1-SNAPSHOT   | 0.1         | 0.2-SNAPSHOT   | Complete |
| carta   | 1.0-SNAPSHOT   | 1.0         | 1.1-SNAPSHOT   | Complete |
