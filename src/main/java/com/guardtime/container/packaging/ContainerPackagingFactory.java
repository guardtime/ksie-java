/*
 * Copyright 2013-2017 Guardtime, Inc.
 *
 * This file is part of the Guardtime client SDK.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES, CONDITIONS, OR OTHER LICENSES OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * "Guardtime" and "KSI" are trademarks or registered trademarks of
 * Guardtime, Inc., and no license to trademarks is granted; Guardtime
 * reserves and retains all trademark rights.
 */

package com.guardtime.container.packaging;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.hash.HashAlgorithmProvider;
import com.guardtime.container.indexing.IncrementingIndexProviderFactory;
import com.guardtime.container.indexing.IndexProvider;
import com.guardtime.container.indexing.IndexProviderFactory;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.manifest.InvalidManifestException;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.manifest.ManifestFactoryType;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.manifest.tlv.TlvContainerManifestFactory;
import com.guardtime.container.packaging.exception.ContainerMergingException;
import com.guardtime.container.packaging.exception.ContainerReadingException;
import com.guardtime.container.packaging.exception.InvalidPackageException;
import com.guardtime.container.packaging.parsing.ContainerReader;
import com.guardtime.container.packaging.parsing.store.ParsingStoreException;
import com.guardtime.container.packaging.parsing.store.ParsingStoreFactory;
import com.guardtime.container.packaging.parsing.store.TemporaryFileBasedParsingStoreFactory;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureException;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.signature.SignatureFactoryType;
import com.guardtime.container.util.DataHashException;
import com.guardtime.container.util.Pair;
import com.guardtime.container.util.Util;
import com.guardtime.container.verification.ContainerVerifier;
import com.guardtime.container.verification.VerifiedContainer;
import com.guardtime.container.verification.policy.InternalVerificationPolicy;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.ksi.hashing.DataHash;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.guardtime.container.packaging.MimeType.MIME_TYPE_ENTRY_NAME;

/**
 * Creates or parses {@link Container} instances.
 */
public class ContainerPackagingFactory {

    private static final Logger logger = LoggerFactory.getLogger(ContainerPackagingFactory.class);

    private final String containerMimeType;

    private final SignatureFactory signatureFactory;
    private final ContainerManifestFactory manifestFactory;
    private final IndexProviderFactory indexProviderFactory;
    private final boolean disableVerification;
    private final ContainerWriter containerWriter;
    private final ContainerReader containerReader;

    private ContainerPackagingFactory(Builder builder) {
        Util.notNull(builder.signatureFactory, "Signature factory");
        Util.notNull(builder.manifestFactory, "Manifest factory");
        Util.notNull(builder.indexProviderFactory, "Index provider factory");
        Util.notNull(builder.parsingStoreFactory, "Parsing store factory");
        Util.notNull(builder.mimeType, "MIME type");
        Util.notNull(builder.containerReader, "Container reader");
        Util.notNull(builder.containerWriter, "Container writer");
        this.signatureFactory = builder.signatureFactory;
        this.manifestFactory = builder.manifestFactory;
        this.indexProviderFactory = builder.indexProviderFactory;
        this.disableVerification = builder.disableInternalVerification;
        this.containerMimeType = builder.mimeType;
        this.containerWriter = builder.containerWriter;
        this.containerReader = builder.containerReader;
        logger.info("Container factory initialized");
    }

    /**
     * Parses an {@link InputStream} to produce a {@link Container}.
     *
     * @param inputStream    An {@link InputStream} that contains a valid/parsable {@link Container}. This InputStream will be
     *                       closed after reading.
     * @return An instance of {@link Container} based on the data from {@link InputStream}. Does not verify
     *         the container/signature(s).
     * @throws InvalidPackageException      When the {@link InputStream} does not contain a parsable {@link Container}.
     * @throws ContainerReadingException    When there were issues parsing some elements of the {@link Container}. The parsed
     *         container and all encountered exceptions can be retrieved from this exception.
     */
    public Container read(InputStream inputStream) throws InvalidPackageException {
        Util.notNull(inputStream, "Input stream");
        try {
            return containerReader.read(inputStream);
        } catch (IOException e) {
            throw new InvalidPackageException("Failed to parse InputStream", e);
        } catch (ParsingStoreException e) {
            throw new InvalidPackageException("Failed to create parsing store for container data", e);
        }
    }

    /**
     * Provides the MIMETYPE content for container.
     */
    public byte[] getMimeTypeContent() {
        return containerMimeType.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Creates a {@link Container} with the input documents and annotations and a signature covering them.
     *
     * @param files          List of {@link ContainerDocument} to be added and signed. Can NOT be null.
     * @param annotations    List of {@link ContainerAnnotation} to be added and signed. Can be null.
     * @return A new {@link Container} which contains the documents and annotations and a signature covering them.
     * @throws InvalidPackageException  When the input data can not be processed or signing fails.
     */
    public Container create(List<ContainerDocument> files, List<ContainerAnnotation> annotations) throws InvalidPackageException {
        SignatureContent signatureContent = verifyAndSign(files, annotations, null);
        MimeTypeEntry mimeType = new MimeTypeEntry(MIME_TYPE_ENTRY_NAME, getMimeTypeContent());
        Container container = new Container(signatureContent, mimeType, containerWriter);
        verifyContainer(container);
        return container;
    }

    /**
     * Creates a {@link SignatureContent} that contains the new set of
     * documents, annotations and a signature for the added elements and adds it to the {@param existingContainer}.
     *
     * @param existingContainer    An instance of {@link Container} which already has
     *                             {@link com.guardtime.container.signature.ContainerSignature}(s)
     * @param files                List of {@link ContainerDocument} to be added and signed. Can NOT be null.
     * @param annotations          List of {@link ContainerAnnotation} to be added and signed. Can be null.
     * @throws InvalidPackageException When the input data can not be processed or signing fails.
     * @throws ContainerMergingException When there are issues adding the newly created {@link SignatureContent} to {@param existingContainer}.
     */
    public void addSignature(Container existingContainer, List<ContainerDocument> files, List<ContainerAnnotation> annotations)
            throws InvalidPackageException, ContainerMergingException {
        Util.notNull(existingContainer, "Container");
        SignatureContent signatureContent = verifyAndSign(files, annotations, existingContainer);
        existingContainer.add(signatureContent);
        verifyContainer(existingContainer);
    }

    private SignatureContent verifyAndSign(List<ContainerDocument> files, List<ContainerAnnotation> annotations,
                                           Container existingContainer) throws InvalidPackageException {
        Util.notEmpty(files, "Document files");
        HashSet<ContainerDocument> documents = new HashSet<>(files);

        IndexProvider indexProvider;
        if (existingContainer != null) {
            for (SignatureContent content : existingContainer.getSignatureContents()) {
                documents.addAll(content.getDocuments().values());
            }
            indexProvider = indexProviderFactory.create(existingContainer);
        } else {
            indexProvider = indexProviderFactory.create();
        }
        verifyNoDuplicateDocumentNames(documents);

        try {
            return new ContentSigner(
                    files,
                    annotations,
                    indexProvider,
                    manifestFactory,
                    signatureFactory
            ).sign();
        } catch (DataHashException | InvalidManifestException e) {
            throw new InvalidPackageException("Failed to create internal structure!", e);
        } catch (SignatureException e) {
            throw new InvalidPackageException("Failed to acquire signature!", e);
        }
    }

    private void verifyContainer(Container container) throws InvalidPackageException {
        if (disableVerification) {
            return;
        }
        VerifiedContainer result = new ContainerVerifier(new InternalVerificationPolicy(this)).verify(container);
        if (!result.getVerificationResult().equals(VerificationResult.OK)) {
            try {
                container.close();
            } catch (Exception e) {
                logger.warn("Failed to clean up after created container that did not pass internal verification.", e);
            }
            for(RuleVerificationResult res : result.getResults()) {
                if(res.getVerificationResult().equals(VerificationResult.NOK)) {
                    logger.error("Failed rule '{}' for '{}' ", res.getRuleName(), res.getTestedElementPath());
                }
                if(res.getVerificationResult().equals(VerificationResult.WARN)) {
                    logger.warn("Failed rule '{}' for '{}' ", res.getRuleName(), res.getTestedElementPath());
                }
            }
            throw new InvalidPackageException("Created Container did not pass internal verification");
        }
    }

    private void verifyNoDuplicateDocumentNames(Set<ContainerDocument> documents) throws IllegalArgumentException {
        Set<String> uniqueDocumentNames = new HashSet<>();
        for (ContainerDocument doc : documents) {
            uniqueDocumentNames.add(doc.getFileName());
        }

        if (uniqueDocumentNames.size() < documents.size()) {
            throw new IllegalArgumentException("Found multiple documents with same name!");
        }
    }

    private static class ContentSigner {

        private List<ContainerDocument> documents;
        private List<ContainerAnnotation> annotations;
        private EntryNameProvider nameProvider;
        private ContainerManifestFactory manifestFactory;
        private SignatureFactory signatureFactory;

        private List<Pair<String, ContainerAnnotation>> annotationPairs = new LinkedList<>();
        private List<Pair<String, SingleAnnotationManifest>> singleAnnotationManifestPairs = new LinkedList<>();
        private Map<String, Pair<ContainerAnnotation, SingleAnnotationManifest>> annotationsManifestContent = new HashMap<>();

        public ContentSigner(List<ContainerDocument> documents, List<ContainerAnnotation> annotations, IndexProvider indexProvider,
                             ContainerManifestFactory manifestFactory, SignatureFactory signatureFactory) {
            this.documents = documents;
            this.annotations = annotations;

            this.manifestFactory = manifestFactory;
            this.signatureFactory = signatureFactory;
            this.nameProvider = new EntryNameProvider(
                    manifestFactory.getManifestFactoryType().getManifestFileExtension(),
                    signatureFactory.getSignatureFactoryType().getSignatureFileExtension(),
                    indexProvider
            );
        }

        public SignatureContent sign() throws InvalidManifestException, SignatureException, DataHashException {
            ManifestFactoryType manifestFactoryType = manifestFactory.getManifestFactoryType();
            SignatureFactoryType signatureFactoryType = signatureFactory.getSignatureFactoryType();
            logger.info("'{}' is used to create and read container manifests", manifestFactoryType.getName());
            logger.info("'{}' is used to create and read container signatures", signatureFactoryType.getName());
            Pair<String, DocumentsManifest> documentsManifest =
                    Pair.of(nameProvider.nextDocumentsManifestName(), manifestFactory.createDocumentsManifest(documents));
            processAnnotations(documentsManifest);
            AnnotationsManifest annotationsManifest = manifestFactory.createAnnotationsManifest(annotationsManifestContent);
            Pair<String, AnnotationsManifest> annotationsManifestPair =
                    Pair.of(nameProvider.nextAnnotationsManifestName(), annotationsManifest);

            Manifest manifest = manifestFactory.createManifest(documentsManifest, annotationsManifestPair,
                    Pair.of(nameProvider.nextSignatureName(), signatureFactoryType.getSignatureMimeType()));


            DataHash hash = getSignatureContentSigningHash(manifest);
            ContainerSignature signature = signatureFactory.create(hash);
            SignatureContent signatureContent = new SignatureContent.Builder()
                    .withDocuments(documents)
                    .withDocumentsManifest(documentsManifest)
                    .withAnnotations(annotationPairs)
                    .withSingleAnnotationManifests(singleAnnotationManifestPairs)
                    .withAnnotationsManifest(annotationsManifestPair)
                    .withManifest(Pair.of(nameProvider.nextManifestName(), manifest))
                    .withSignature(signature)
                    .build();
            return signatureContent;
        }

        private DataHash getSignatureContentSigningHash(Manifest manifest) throws DataHashException {
            HashAlgorithmProvider algorithmProvider = manifestFactory.getHashAlgorithmProvider();
            return manifest.getDataHash(algorithmProvider.getSigningHashAlgorithm());
        }

        private void processAnnotations(Pair<String, DocumentsManifest> documentsManifest) throws InvalidManifestException {
            if (annotations == null) {
                return;
            }
            for (ContainerAnnotation annotation : annotations) {
                Pair<String, ContainerAnnotation> annotationPair =
                        Pair.of(nameProvider.nextAnnotationDataFileName(), annotation);
                annotationPairs.add(annotationPair);
                SingleAnnotationManifest singleAnnotationManifest =
                        manifestFactory.createSingleAnnotationManifest(documentsManifest, annotationPair);
                String annotationManifestName = nameProvider.nextSingleAnnotationManifestName();
                singleAnnotationManifestPairs.add(Pair.of(annotationManifestName, singleAnnotationManifest));
                annotationsManifestContent.put(annotationManifestName, Pair.of(annotation, singleAnnotationManifest));
            }
        }

    }

    public static class Builder {

        protected SignatureFactory signatureFactory;
        protected ContainerManifestFactory manifestFactory = new TlvContainerManifestFactory();
        protected boolean disableInternalVerification = false;
        protected IndexProviderFactory indexProviderFactory = new IncrementingIndexProviderFactory();
        protected ParsingStoreFactory parsingStoreFactory = new TemporaryFileBasedParsingStoreFactory();
        protected String mimeType;
        protected ContainerWriter containerWriter;
        protected ContainerReader containerReader;

        public Builder withSignatureFactory(SignatureFactory factory) {
            this.signatureFactory = factory;
            return this;
        }

        public Builder withManifestFactory(ContainerManifestFactory factory) {
            this.manifestFactory = factory;
            return this;
        }

        public Builder withIndexProviderFactory(IndexProviderFactory factory) {
            this.indexProviderFactory = factory;
            return this;
        }

        public Builder withParsingStoreFactory(ParsingStoreFactory factory) {
            this.parsingStoreFactory = factory;
            return this;
        }

        public Builder disableInternalVerification() {
            this.disableInternalVerification = true;
            return this;
        }

        public Builder withMimeTypeString(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public Builder withContainerWriter(ContainerWriter writer) {
            this.containerWriter = writer;
            return this;
        }

        public Builder withContainerReader(ContainerReader reader) {
            this.containerReader = reader;
            return this;
        }

        public Builder enableInternalVerification() {
            this.disableInternalVerification = false;
            return this;
        }

        public ContainerPackagingFactory build() throws IOException {
            return new ContainerPackagingFactory(this);
        }
    }

}
