# Artifact Repositories - 2019-04-15

Artifact repositories come in all shapes and sizes. Even version control systems
can be, and are, used as artifact repositories. There are several challenges in 
defining a repository. Two main challenges are storage and indexing. Storage is 
important because it needs to be able to handle large amounts of artifacts with 
the metadata needed to find the artifact. Indexing is important to be able to 
find the artifacts efficiently. There are also two main actions that need to be 
considered, publishing and retrieving. Both actions are important and require 
specific desired outcomes. Publishing should result in new artifacts being 
published and indexed while retrieving should result in an efficient way to find 
and retrieve artifacts.

## Examples
Several repository examples are available to examine. Three will be considered 
here: Git, Maven and Debian. First, the commonalities are identified. Each 
system is capable of storing vast amounts of artifacts efficiently with some 
metadata allowing for organization and indexing. Specific examples of common 
metadata are, repository, artifact name and artifact version. Each has a way to 
index the artifacts and each has the ability to retrieve artifacts with common 
protocols (http and file). Some differences also exist. Git, for example, is
best used with a Git client to manage publishing and retrieving artifacts. Maven
uses a client for publishing but retrieving artifacts can usually be 
accomplished without the need of a client. Debian repositories are a bit more 
complex and could be managed without a specific client, but it would be tedious.
Debian repositories are also easily accessible without the need of a client.

## Desired Features
The requirements of a repository for ${project.name} are similar to the example 
repositories but are slightly skewed due to the lower number of artifacts and 
users as well as the higher ratio of providers to users. The following is a 
non-exhaustive list of desired features:
- The ability for providers to publish to a common repository or an individual 
repository with little effort for small numbers of artifacts.
- The ability to provide links in common markets to artifacts in any repository.
- The ability to find artifacts efficiently.
- The ability to host repositories via file, zip or http protocol.
- The ability to identify artifacts by version, channel, platform and 
architecture.
- The ability to provide other metadata without needing to acquire the entire 
artifact.

## Operations
There are two main aspects to consider regarding operations that add complexity 
to the repository solution, publishing and retrieving. An example of this
dichotomy is Git. Retrieving artifacts from most Git repositories online is 
pretty simple with
HTTP or SSH. However, updating the repository is not that simple and is usually
done with a Git client. An optimal solution would make it simple for both 
operations but there is going to be a balance. Either the publisher needs to 
have the logic to maintain indexes and metadata, the repository, or the client. 
However, the client experience is important and should be made as
effortless as reasonably possible.

## Other Considerations

There are some other considerations that should be identified. 

### Channels
One consideration
is whether to treat channels as special versions, versions as special channels 
or consider them entirely separate. The idea behind a channel is to be able to
request the latest in a category such as "nightly", "stable", "beta" or other
types of category. Ways to implement a channel are described later. 
### Metadata
Another 
consideration is where to put metadata. Metadata could be stored in the index,
stored "near" the artifact or even be part of the path to an artifact. There may
even be metadata in all of these locations.
### Storage
Another consideration is storage. The amount of storage space can add up over
time if the artifacts are not removed on occasion. If stored in a Git repository
the size of the repository could be considerable over time. 

## Pros and Cons

| System | Purpose | Pros | Cons |
|---|---|---|---|
| Git | Version control repository | Natively handles multiple versions |Complex repository structure that is best used with a Git client |
|  |  | HTTP and SSH access | No extra metadata |
|  |  | Flexible folder layout |  |
|  |  |  |  |
| Maven | Artifact repository | Native build tool| No concept for channels |
|  |  | Simple repository structure | No extra metadata |
|  |  | Indexes |  |
|  |  |  |  |
| Debian | Operating system repository | Very capable | More complex than Maven |
|  |  | Indexes |  |
|  |  | Metadata |  |
|  |  | Artifact pool |  |
|  |  |  |  |

# Xeomar Artifact Repository

After the above examination, the question remains how to implement the Xeomar
artifact repository. It has been proposed that some portions of the repository
be stored in Git to allow others to propose additions to the "official"
repository. However, Git is not likely to be the complete solution due to 
artifact storage needs. Git just isn't a good way to store a large number of 
binary artifacts. A managed file system is probably more appropriate for that.

Data retrieval is the other side of the coin that needs to be addressed. Data is
retrieved for several purposes, not just downloading an artifact:
- Determine what catalogs are available
- Download one or more catalogs
- Determine what products/artifacts are available
- Download metadata for one or more products/artifacts
- Determine what versions of a specific product/artifact are available
- Download specific versions of a product/artifact

It has also been proposed that categorical versions like latest, stable, beta, 
etc. be separate catalogs/repositories. This makes some actions simpler while 
others more challenging. It does, however, follow the pattern of many popular
Linux distributions.

