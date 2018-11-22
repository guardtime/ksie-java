# KSIE Java SDK

Guardtime's KSI Blockchain is an industrial scale blockchain platform that cryptographically ensures data integrity and proves time of existence. The KSI signatures, based on hash chains, link data to this global calendar blockchain.

The checkpoints of the blockchain, published in newspapers and electronic media, enable long term integrity of any digital asset without the need to trust any system. There are many applications for KSI, a classical example is signing of any type of logs - system logs, financial transactions, call records, etc, but also any other type of documents and data. For more, see https://guardtime.com

KSI Envelope (KSIE) is designed to contain data, meta-data and KSI signatures. The signed data may be detached from the envelope or attached to the envelope itself. The KSI Envelope supports two kinds of custom meta-data: meta-data that can be removed and meta-data that can not be removed from it without affecting the verification result.

The KSIE Java SDK is a software development kit for developers who want to integrate KSIE with their Java based applications and systems. It provides an API for all KSIE functionality.

Access to full KSIE specification can be requested from https://guardtime.com/blockchain-developers


## Installation

In order to get the latest version of KSIE Java SDK, download the source and build using Maven.


## Compiling the Code

To compile the code you need JDK 1.7 (or later) and [Maven 3](https://maven.apache.org/).
The project can be built via the command line by executing the following maven command:

```
mvn clean install
```

This command instructs Maven to build and install the project in the local repository. Also all integration and unit tests will be run.

In order to run the integration tests successfully you need to have access to KSI
service, the simplest is to request a trial account here [https://guardtime.com/blockchain-developers](https://guardtime.com/blockchain-developers).

Add the KSI configuration to the file `src/test/resources/config.properties` (see file `src/test/resources/config.properties.sample` for more information).

You can skip the integration tests by executing the following command:

```
mvn clean install -DskipITs
```

You can skip unit and integration tests by executing the following command:

```
mvn clean install -DskipTests
```


## Usage

The API full reference is available at: [http://guardtime.github.io/ksie-java/](http://guardtime.github.io/ksie-java/).

For most activities you need to have previously composed `EnvelopePackagingFactory` which requires a `SignatureFactory` and an `EnvelopeReader`.

Optionally other specifiers can be set:
* `EnvelopeManifestFactory` can be provided if the standard TLV based manifest factory is not desired.
* `IndexProviderFactory` can be provided to indicate what signature index string should be used.
* `ParsingStore` can be provided to indicate where parsed envelope data will be stored during runtime.

Following is the example of creating a packaging factory for ZIP based envelopes with TLV manifest structures and KSI signatures, which will be the basis for the rest of the code examples:

```java
// Declare KSI Signer and Reader
Signer signer;
Reader reader;
/* Initialize KSI Signer and Reader
...
*/

SignatureFactory signatureFactory = new KsiSignatureFactory(signer, reader);
EnvelopePackagingFactory packagingFactory = new ZipEnvelopePackagingFactoryBuilder().withSignatureFactory(signatureFactory).build();
/*
Note that the EnvelopeReader is not specified in this example as the
ZipEnvelopePackagingFactoryBuilder provides its own.
*/
```

### Creating the Envelope

In order to create a new envelope you have a choice of using the `EnvelopeBuilder` as shown below:

```java
EnvelopeBuilder builder = new EnvelopeBuilder(packagingFactory);
builder.withDocument(...);      // Can be used multiple times before calling build()
builder.withAnnotation(...);    //Can be used multiple times before calling build() or can be omitted
Envelope signedEnvelope = builder.build();
```

Or you can use the `EnvelopePackagingFactory` directly as shown below:

```java
List<Document> documents;
List<Annotation> annotations;  // Can be empty list
/* Initialize and fill documents and annotations lists
...
*/
Envelope signedEnvelope = packagingFactory.create(documents, annotations);
```

It is important to remember that by default each time a KSI envelope is created, its content is also signed. Thus you always get a signed envelope from both, the `EnvelopeBuilder` and the `EnvelopePackagingFactory`. This behaviour can be altered by using a different `SignatureFactory`. For example by using the `PostponedSignatureFactory`.

When trying to parse an existing envelope only the `packagingFactory` can be used as shown below:

```java
Envelope parsedEnvelope = packagingFactory.read(inputStream);
```

It is suggested to always verify the parsed envelope before adding new documents or annotations to it, see instructions in Verifying the Envelope section.


### Writing the Envelope

In order to write an envelope to some storage medium an `EnvelopeWriter` is needed.
From existing implementations `ZipEnvelopeWriter` can be used from `com.guardtime.envelope.packaging.zip` package.

```java
OutputStream outputStream = new FileOutputStream("path/to/store/envelope.extension");
EnvelopeWriter writer = new ZipEnvelopeWriter();
writer.write(envelope, outputStream);
```


### Adding New Documents or Annotations to the Existing Envelope

Both, the `EnvelopeBuilder` and `EnvelopePackagingFactory` allow adding new documents and annotations to the existing envelope. The existing envelope's content will be copied to a new envelope and that will be expanded with the new documents/annotations and a signature covering them.

With `EnvelopeBuilder`:

```java
EnvelopeBuilder builder = new EnvelopeBuilder(packagingFactory);
builder.withExistingEnvelope(parsedEnvelope);
builder.withDocument(...);      // Can be used multiple times before calling build()
builder.withAnnotation(...);    // Can be used multiple times before calling build() or can be omitted
Envelope signedEnvelope = builder.build();
```

With `EnvelopePackagingFactory`:

```java
List<Document> documents;
List<Annotation> annotations;  // Can be empty list
/* Initialize and fill documents and annotations lists
...
*/
Envelope expandedEnvelope = packagingFactory.addSignature(parsedEnvelope, documents, annotations);
```

### Merging the Envelopes

`SignatureContent`s can be merged from one `Envelope` to another.

``` java
Envelope base = packagingFactory.read(inputStream);
Envelope env1 = packagingFactory.read(anotherInputStream);
// Add all or some depending on the need.
base.addAll(env1.getSignatureContents());
//base.add(env1.getSignatureContents().get(0));
env1.close()
```

### Extending Signatures in the Envelope ###

For extending it is necessary to specify the `SignatureFactory` implementation that applies to the given envelope, and the `ExtendingPolicy` to define extension point.

To extend all the KSI signatures in an envelope:

```java
Extender extender;
/* Initialize KSI Extender
...
com.guardtime.ksi.Extender extender
...
*/
ExtendingPolicy extendingPolicy = new KsiEnvelopeSignatureExtendingPolicy(extender)
EnvelopeSignatureExtender signatureExtender = new EnvelopeSignatureExtender(signatureFactory, extendingPolicy)
ExtendedEnvelope extendedEnvelope = signatureExtender.extend(envelope);

// Check extended status either for Envelope or per SignatureContent:
extendedEnvelope.isExtended();
extendedEnvelope.getExtendedSignatureContents().get(0).isExtended();
```


### Verifying the Envelope

The following example shows a simple verification of an envelope:

```java
ContextAwarePolicy contextAwarePolicy;
/* Initialize KSI verification context e.g. ContextAwarePolicyAdapter.createInternalPolicy()
...
*/
List<Rule> implicitRules;
/* Initialize array and specify any rules deemed missing and necessary from the DefaultVerificationPolicy
...
*/
Rule signatureRule = new KsiPolicyBasedSignatureIntegrityRule(contextAwarePolicy);
DefaultVerificationPolicy policy = new DefaultVerificationPolicy(signatureRule, new MimeTypeIntegrityRule(packagingFactory), implicitRules);
EnvelopeVerifier verifier = new EnvelopeVerifier(policy);
VerifiedEnvelope verifiedEnvelope = verifier.verify(envelope);

// View verification result either for whole Envelope or per SignatureContent:
VerificationResult verificationResult = verifiedEnvelope.getVerificationResult(); // OK/NOK/WARN
VerificationResult verificationResult = verifiedEnvelope.getVerifiedSignatureContents().get(0).getVerificationResult(); // OK/NOK/WARN
```

Since currently there are no reports for failed verification, you need to loop through the raw results to get a more detailed overview of what was the reason for verification error.

```java
for(RuleVerificationResult ruleResult : result.getResults()) {
    ruleResult.getVerificationResult();     // OK/NOK/WARN
    ruleResult.getRuleName();               // What rule produced the result
    ruleResult.getTestedElementPath();      // What element was tested for the result.
}
```

### Closing the Envelope

`Envelope`, `SignatureContent`, `Document` and `Annotation` are derived from `AutoCloseable` since they may hold resources which need to be closed once they are no longer needed. Therefore calling `close()` is highly recommended to avoid any data leaks.

Calling `close()` on an `Envelope` will also close all `SignatureContent`, `Document` and `Annotation` that it has references to.


## Dependencies

See Maven `pom.xml` files or use the following Maven command

```
mvn dependency:tree
```

## Compatibility

Java 1.7 or later.

## Contributing

See `CONTRIBUTING.md` file.

## License

See `LICENSE` file.
