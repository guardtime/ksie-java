# Introduction

The aim of this project is to implement a container format for associating data object(s) and metadata with blockchain based signatures. 

# Requirements

* Java 1.7
* Maven 3.x 
* KSI Java SDK 4.2.32

# Usage

For many activities you need to have composed a ContainerPackagingFactory which in most/all cases requires a ManifestFactory and a SignatureFactory.
Here is an example of creating a packaging factory for ZIP based containers with TLV manifest structures and KSI based signatures, which will be the basis for the rest of the code examples:

```java
KSI ksi;
/* Initialize KSI
...
*/
ContainerManifestFactory manifestFactory = new TlvContainerManifestFactory();
SignatureFactory signatureFactory = new KsiSignatureFactory(ksi);
ZipContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactory(signatureFactory, manifestFactory);
```
## Creating a container

In order to create a new container you have a choice of using the ContainerBuilder as shown below:

```java
ContainerBuilder builder = new ContainerBuilder(packagingFactory);
builder.withDataFile(...);      //can be used multiple times before calling build()
builder.withAnnotation(...);    //can be used multiple times before calling build()
Container signedContainer = builder.build();
```

Or you can use the packaging factory directly as shown below:

```java
List<ContainerDocument> documents;
List<ContainerAnnotation> annotations;
/* initialize and fill documents and annotations lists
...
*/
Container signedContainer = packagingFactory.create(documents, annotations);
```

It is important to note that creating a container also signs its contents so you always get a signed container from both the ContainerBuilder and the ContainerPackagingFactory.

When trying to parse an existing container only the packagingFactory can be used as shown below:

```java
Container parsedContainer = packagingFactory.read(inputStream);
```

It is suggested to always verify the parsed container before adding new documents/annotations to it.

## Adding new documents/annotations to existing container

Both the ContainerBuilder and ContainerPackagingFactory allow for adding new documents and annotations to an existing container.
The existing container will be expanded with the new documents/annotation and a signature covering them.

ContainerBuilder:

```java
ContainerBuilder builder = new ContainerBuilder(packagingFactory);
builder.withExistingContainer(parsedContainer);
builder.withDataFile(...);      //can be used multiple times before calling build()
builder.withAnnotation(...);    //can be used multiple times before calling build()
Container signedContainer = builder.build();
```

ContainerPackagingFactory:

```java
List<ContainerDocument> documents;
List<ContainerAnnotation> annotations;
/* initialize and fill documents and annotations lists
...
*/
Container signedContainer = packagingFactory.create(parsedContainer, documents, annotations);
```

## Extending signatures in a container

For extending it is necessary to specify the SignatureExtender implementation that applies to the given container.

The following example shows extending of all signatures in a container.

```java
SignatureExtender signatureExtender = new KsiSignatureExtender(ksi)
ContainerExtender extender = new ContainerExtender(signatureExtender);
Container extendedContainer = extender.extend(container);
```

## Verifying a container

The following example shows a simple verification for a container.

```java
VerificationContext context = new SimpleVerificationContext(container);
List<Rule> implicitRules;
/* Initialize array and specify any rules deemed missing and necessary from the DefaultVerificationPolicy
...
*/
DefaultVerificationPolicy policy = new DefaultVerificationPolicy(implicitRules);
ContainerVerifier verifier = new ContainerVerifier(policy);
VerificationResult result = verifier.verify(context);
```