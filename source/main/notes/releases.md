# Xenon Release Process

A Xenon release is expected to have all the dependencies released as well, 
including Maven parent poms and Maven plugins. This amounts to nearly a dozen 
different projects that need to be released, one by one, leading up to the Xenon 
release. After Xenon is released, then Xenon modules can be updated and released.
This document helps coordinate the process:

1. For each dependency project
    1. Update dependency versions to non-SNAPSHOT versions, build and test
    1. Set the project version to a non-SNAPSHOT version, build and test
    1. Commit and push the version changes, this should trigger the release build
    1. Update the project version to the next SNAPSHOT version
    1. Update any dependency versions if desired to the next version
    1. Before committing and pushing, be sure the release build is complete and successful
    1. Commit and push the version changes, this should trigger a new build

## Dependency Release List for Version 1.9

| Project               | Old Version   | New Version |   Status | When       |
|-----------------------|---------------|-------------|---------:|------------|
| Maven Parent POMs     |
| top                   | 3.6.4         | 3.6.4       | Complete | 2025-09-25 |
| jar                   | 3.6.4         | 3.6.4       | Complete | 2025-09-25 |
| asm                   | 3.6.4         | 3.6.4       | Complete | 2025-09-25 |
| prd                   | 3.6.4         | 3.6.4       | Complete | 2025-09-25 |
| prg                   | 3.6.4         | 3.6.4       | Complete | 2025-09-25 |
| mod                   | 3.6.4         | 3.6.4       | Complete | 2025-09-25 |
|                       |               |             |          |
| Avereon Libraries     |
| zevra                 | 0.11-SNAPSHOT | 0.11        | Complete | 2025-09-25 |
| zerra                 | 0.11-SNAPSHOT | 0.11        |          |            |
| zenna                 | 0.11-SNAPSHOT | 0.11        |          |            |
| curve                 | 0.4-SNAPSHOT  | 0.4         |          |            |
| marea                 | 0.2-SNAPSHOT  | 0.2         |          |            |
|                       |               |             |          |            |
| Avereon Maven Plugins |
| curex                 | 1.5.0         | 1.5.0       | Complete | 2025-09-25 |
| cameo                 | 2.12-SNAPSHOT | 2.12        |          |            |
|                       |               |             |          |            |
| Avereon Applications  |
| weave                 | 1.6-SNAPSHOT  | 1.6         |          |            |
| xenon                 | 1.9-SNAPSHOT  | 1.9         |          |            |
|                       |               |             |          |            |
| Xenon Test Library    |
| xenos                 | 1.9-SNAPSHOT  | 1.9         |          |            |
|                       |               |             |          |            |
| Avereon Mods          |
| acorn                 | 1.2.4         | 1.3         |          |
| aveon                 | 1.2-SNAPSHOT  | 1.2         |          |
| carta                 | 1.3-SNAPSHOT  | 1.3         |          |
| mazer                 | 1.3-SNAPSHOT  | 1.3         |          |
| recon                 | 1.2-SNAPSHOT  | 1.2         |          |
| sysup                 |               | 1.0.0       |          |
