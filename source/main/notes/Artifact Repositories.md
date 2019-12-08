# Artifact Repository Notes - 2019-04-15

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

# Avereon Artifact Repository

After the above examination, the question remains how to implement the Avereon
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

## Use Cases
- Program wants to know the available products in all markets, regardless of channel
  - Program requests a list of unique products in each market
  - The market returns the latest release for each product it hosts
  - The program will need to determine how it will handle products found in multiple markets
    - Usually by finding the latest release for each unique product
- [Check for updates] Program wants to know the latest release of a product, probably by channel
  - The program already knows what market and product to look for, it just needs to know what the latest version is
  - The market returns the product card for the latest release of the product it hosts
  - If the product is no longer hosted in the expected market (HTTP 404) it may inform the user
    - And might find new versions of the product in other markets and also inform the user
- [Stage updates] Program wants to download a specific artifact of a product
  - The program has already determined what it needs to download and asks the market for the specific artifact to download
  - The market returns the product artifact requested, or HTTP 404 if it is not found
- Web site wants to know the available downloads for a product, probably by channel, category(installer) and platform
  - The web site requests a list of the available downloads
    - It might query for all products, products in a channel, channels for a product, artifacts for a product, etc.
  - The market returns a list of product downloads
- Web site wants to download a specific artifact of a product, probably the installer
  - The web site has already determined what it needs to download and asks the market for the specific artifact to download
  - The market returns the product artifact requested, or HTTP 404 if it is not found

Based on the use cases both the client and server could have logic to help the
other. The question now stands, how much logic to put where? There is no
question the client needs some logic to determine what it wants, but should it
also have logic to handle a "dumb" repository? And how "dumb" can the
repository be before it becomes a problem? How much burden do we require of
repository maintainers to have "smart" repositories?

## Repositories
It's pretty easy these days to create a repository that is fairly "smart",
especially if the repository is available on the Internet. Because the language
of the Internet is HTTP then it can be used to provide a smarter repository
experience. Not only that, but it can provide multiple experiences over time
and even at the same time for smooth transitions. By making a well defined and
hopefully simple set of rules how a client will interact with a repository and
determine what assets it wants then a reasonable repository can be defined.

    namespace (helps distribute file structure)
    name
    version
    platform [os/arch]
    qualifier? classifier?

Pattern:

    /root/namespace/name/version/os/arch/product.file

Examples:

    /pool/com/avereon/xenon/0.8-20190414034312/any/any/install.pack
    /pool/com/avereon/xenon/0.8-20190414034312/any/any/product.card
    /pool/com/avereon/xenon/0.8-20190414034312/any/any/product.pack

Terminology

Market - Where to get a list of catalogs\
Store - The market storage location of artifacts\
Catalog - Where to get a list of products\
Product - The collection of artifacts (many products only have one)\
Artifact - A specific distributable file

If "stores" are referenced by "channel" then they only have to have one copy of
the artifact. The metadata regarding the artifact still needs to be available,
but there only needs to be one. And, arguably could be stored by UUID.

CI/CD Notes

write >  
commit - store the code in an scm >\
build - run through the CI pipeline >\
publish - run through the CD pipeline >\
check - find published updates in the repo >\
download - download the update from the repo >\
install - install the update


