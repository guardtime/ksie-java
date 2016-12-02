# Introduction

The aim of this project is to implement a container format for associating data object(s) and metadata with blockchain based signatures. 

# Requirements

* Java 1.7
* Maven 3.x 
* KSI Java SDK 4.4.67

# Usage

For many activities you need to have composed a ContainerPackagingFactory which requires a SignatureFactory.

Optionally other specifiers can be set:
* ContainerManifestFactory can be provided if the standard TLV based manifest factory is not desired.
* IndexProviderFactory can be provided to indicate what signature index string should be used.
* ParsingStoreFactory can be provided to indicate where parsed Container data will be stored during runtime.

Here is an example of creating a packaging factory for ZIP based containers with TLV manifest structures and KSI based signatures, which will be the basis for the rest of the code examples:

```java
KSI ksi;
/* Initialize KSI
...
*/
SignatureFactory signatureFactory = new KsiSignatureFactory(ksi);
ZipContainerPackagingFactory packagingFactory = new ZipContainerPackagingFactoryBuilder().withSignatureFactory(signatureFactory).build();
```
## Creating a container

In order to create a new container you have a choice of using the ContainerBuilder as shown below:

```java
ContainerBuilder builder = new ContainerBuilder(packagingFactory);
builder.withDocument(...);      //can be used multiple times before calling build()
builder.withAnnotation(...);    //can be used multiple times before calling build()  or can be omitted
Container signedContainer = builder.build();
```

Or you can use the ContainerPackagingFactory directly as shown below:

```java
List<ContainerDocument> documents;
List<ContainerAnnotation> annotations;  // Can be empty list
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
The existing containers content will be copied to a new container and that will be expanded with the new documents/annotation and a signature covering them.

With ContainerBuilder:

```java
ContainerBuilder builder = new ContainerBuilder(packagingFactory);
builder.withExistingContainer(parsedContainer);
builder.withDocument(...);      //can be used multiple times before calling build()
builder.withAnnotation(...);    //can be used multiple times before calling build()  or can be omitted
Container signedContainer = builder.build();
```

With ContainerPackagingFactory:

```java
List<ContainerDocument> documents;
List<ContainerAnnotation> annotations;  // Can be empty list
/* initialize and fill documents and annotations lists
...
*/
Container signedContainer = packagingFactory.create(parsedContainer, documents, annotations);
```

## Extending signatures in a container

For extending it is necessary to specify the SignatureFactory implementation that applies to the given container. 
And the ExtendingPolicy to define extension point.

The following example shows extending of all signatures in a container.

```java
ExtendingPolicy extendingPolicy = new KsiContainerSignatureExtendingPolicy(ksi)
ContainerSignatureExtender signatureExtender = new ContainerSignatureExtender(signatureFactory, extendingPolicy)
extender.extend(container);
```

## Verifying a container

The following example shows a simple verification for a container.

```java
List<Rule> implicitRules;
/* Initialize array and specify any rules deemed missing and necessary from the DefaultVerificationPolicy
...
*/
Rule signatureRule = new KsiPolicyBasedSignatureIntegrityRule(ksi, KeyBasedVerificationPolicy());
DefaultVerificationPolicy policy = new DefaultVerificationPolicy(signatureRule, new MimeTypeIntegrityRule(packagingFactory), implicitRules);
ContainerVerifier verifier = new ContainerVerifier(policy);
ContainerVerifierResult result = verifier.verify(container);
VerificationResult verificationResult = result.getVerificationResult(); // OK/NOK/WARN
```

Since there currently are no reports for verification then you'd have to loop through the raw results to get a more detailed overview of what failed verification.

```java
for(RuleVerificationResult ruleResult : result.getResults()) {
    ruleResult.getVerificationResult();     // OK/NOK/WARN
    ruleResult.getRuleName();               // What rule produced the result
    ruleResult.getTestedElementPath();      // What element was tested for the result.
}
```

## Closing a container

Container, ContainerDocument and ContainerAnnotation are derived from AutoCloseable since they may hold resources which need to be closed once they are no longer needed. 
Therefore calling close() is highly recommended to avoid any data leaks.

Calling close() on a Container will also close all ContainerDocument and ContainerAnnotation that it has references to.