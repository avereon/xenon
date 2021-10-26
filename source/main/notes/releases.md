# Xenon Release Process

A Xenon release is expected to have all the dependencies released as well, including Maven parent poms and Maven plugins. This amounts to nearly a dozen different projects that need to be released,
one by one, leading up to the Xenon release. This document helps coordinate the process:

1. For each dependency project (except Xenon)
    1. Update dependency versions to non-SNAPSHOT versions, build and test
    1. Set the project version to a non-SNAPSHOT version, build and test
    1. Commit and push the version changes, this should trigger the release build
    1. Update the project version to the next SNAPSHOT version
    1. Update any dependency versions if desired to the next version
    1. Before committing and pushing, be sure the release build is complete and successful
    1. Commit and push the version changes, this should trigger a new build
1. For the Xenon release
    1. Create a new “staging” branch from the “working” branch (usually main or master)
        1. ```> git checkout main```
        1. ```> git checkout -b stage```
    1. Update dependency versions to non-SNAPSHOT versions, build and test
    1. Set the project version to a non-SNAPSHOT version, build and test
    1. Commit and push the “staging” branch
        1. ```> git push --set-upstream origin stage```
    1. Create a pull request from the “staging” branch to the release branch (usually stable)
    1. Squash and merge the pull request, this should trigger the release build
    1. Delete the “staging” branch
    1. If desired, locally switch to the “release” branch and update it
        1. ```> git checkout stable```
        1. ```> git pull```
    1. Switch back to the “working” branch and merge the stable commits
        1. ```> git checkout main```
        1. ```> git merge stable```
    1. Set the project version to a SNAPSHOT version, build and test
    1. Update any dependency versions if desired to the next version
    1. Update source/main/jpackage/app.options app-version
    1. Commit and push the version changes, this should trigger a new build

## Dependency Release List for Version 1.5

| Project | Old Version | New Version | Status |
|---|---|---|---:|
| top | 3.2.0-SNAPSHOT | 3.2.0 | Complete |
| jar | 3.2.0-SNAPSHOT | 3.2.0 | Complete |
| prd | 3.2.0-SNAPSHOT | 3.2.0 | Complete |
| prg | 3.2.0-SNAPSHOT | 3.2.0 | Complete |
| mod | 3.2.0-SNAPSHOT | 3.2.0 | Complete |
|  |  |  |  |
| zevra | 0.6-SNAPSHOT | 0.7 | Complete |
| zarra | 0.5-SNAPSHOT | 0.7 | Complete |
| zerra | 0.5-SNAPSHOT | 0.7 | Complete |
| zenna | 0.6-SNAPSHOT | 0.7 | Complete |
|  |  |  |  |  |
| curex | 1.3-SNAPSHOT | 1.3 | Complete |
| cameo | 2.8-SNAPSHOT | 2.9 | Complete |
|  |  |  |  |  |
| weave | 0.5-SNAPSHOT | 1.1 | Complete |
| xenon | 1.5-SNAPSHOT | 1.5 | Complete |
|  |  |  |  |  |
| curve | 0.2-SNAPSHOT | 0.2 | Complete |
| carta | 1.1-SNAPSHOT | 1.1 | Complete |


## Dependency Release List for Version 1.4

| Project | Old Version | Release Version | New Version | Status |
|---|---|---|---|---:|
| top | 3.1.0-SNAPSHOT | 3.1.0 | 3.2.0-SNAPSHOT | Complete |
| jar | 3.1.0-SNAPSHOT | 3.1.0 | 3.2.0-SNAPSHOT | Complete |
| prd | 3.1.0-SNAPSHOT | 3.1.0 | 3.2.0-SNAPSHOT | Complete |
| prg | 3.1.0-SNAPSHOT | 3.1.0 | 3.2.0-SNAPSHOT | Complete |
| mod | 3.1.0-SNAPSHOT | 3.1.0 | 3.2.0-SNAPSHOT | Complete |
|  |  |  |  |  |
| zevra | 0.5-SNAPSHOT | 0.5 | 0.6-SNAPSHOT | Complete |
| zarra | 0.4-SNAPSHOT | 0.4 | 0.5-SNAPSHOT | Complete |
| zerra | 0.4-SNAPSHOT | 0.4 | 0.5-SNAPSHOT | Complete |
| zenna | 0.5-SNAPSHOT | 0.5 | 0.6-SNAPSHOT | Complete |
|  |  |  |  |  |
| curex | 1.2-SNAPSHOT | 1.2 | 1.3-SNAPSHOT | Complete |
| cameo | 2.7-SNAPSHOT | 2.7 | 2.8-SNAPSHOT | Complete |
|  |  |  |  |  |
| weave | 0.4-SNAPSHOT | 0.4 | 0.5-SNAPSHOT | Complete |
| xenon | 1.4-SNAPSHOT | 1.4 | 1.5-SNAPSHOT | Complete |
|  |  |  |  |  |
| curve | 0.1-SNAPSHOT | 0.1 | 0.2-SNAPSHOT | Complete |
| carta | 1.0-SNAPSHOT | 1.0 | 1.1-SNAPSHOT | Complete |