# Introduction

The aim of this project is to implement a container format for associating data object(s) and metadata with blockchain based signatures. 

# Requirements

* Java 1.7
* Maven 3.x 
* KSI Java SDK 4.2.32

# Installation

The latest stable binary releases are available at [http://search.maven.org](http://search.maven.org). Just include the
dependencies in your pom.xml:

```xml
<dependency>
    <groupId>com.guardtime</groupId>
    <artifactId>container</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

# Usage

For all activities you need to have composed a ContainerPackagingFactory which in most/all cases requires a ManifestFactory and a SignatureFactory.

Here is an example of creating a packaging factory for ZIP based containers with TLV manifest structures and KSI based signatures, which will be the basis for the rest of the code examples:

```java
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

## Extending signatures in a container

For extending it is necessary to specify the SignatureExtender implementation that applies to the given container.

The following example extends all signatures in the container.

```java
SignatureExtender signatureExtender = new KsiSignatureExtender(ksi)
ContainerExtender extender = new ContainerExtender(signatureExtender);
Container extendedContainer = extender.extend(container);
```

## Verifying a container

The following example shows a simple verification for container.

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