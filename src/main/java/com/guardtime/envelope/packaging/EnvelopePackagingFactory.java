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

package com.guardtime.envelope.packaging;

import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.document.Document;
import com.guardtime.envelope.hash.HashAlgorithmProvider;
import com.guardtime.envelope.indexing.IncrementingIndexProviderFactory;
import com.guardtime.envelope.indexing.IndexProvider;
import com.guardtime.envelope.indexing.IndexProviderFactory;
import com.guardtime.envelope.manifest.AnnotationsManifest;
import com.guardtime.envelope.manifest.DocumentsManifest;
import com.guardtime.envelope.manifest.EnvelopeManifestFactory;
import com.guardtime.envelope.manifest.InvalidManifestException;
import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.manifest.ManifestFactoryType;
import com.guardtime.envelope.manifest.SingleAnnotationManifest;
import com.guardtime.envelope.manifest.tlv.TlvEnvelopeManifestFactory;
import com.guardtime.envelope.packaging.exception.EnvelopeMergingException;
import com.guardtime.envelope.packaging.exception.EnvelopeReadingException;
import com.guardtime.envelope.packaging.exception.InvalidPackageException;
import com.guardtime.envelope.packaging.parsing.EnvelopeReader;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreException;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreFactory;
import com.guardtime.envelope.packaging.parsing.store.TemporaryFileBasedParsingStoreFactory;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.signature.SignatureException;
import com.guardtime.envelope.signature.SignatureFactory;
import com.guardtime.envelope.signature.SignatureFactoryType;
import com.guardtime.envelope.util.DataHashException;
import com.guardtime.envelope.util.Pair;
import com.guardtime.envelope.util.Util;
import com.guardtime.envelope.verification.EnvelopeVerifier;
import com.guardtime.envelope.verification.VerifiedEnvelope;
import com.guardtime.envelope.verification.policy.InternalVerificationPolicy;
import com.guardtime.envelope.verification.result.RuleVerificationResult;
import com.guardtime.envelope.verification.result.VerificationResult;
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

/**
 * Creates or parses {@link Envelope} instances.
 */
public class EnvelopePackagingFactory {

    private static final Logger logger = LoggerFactory.getLogger(EnvelopePackagingFactory.class);

    private final String envelopeMimeType;

    private final SignatureFactory signatureFactory;
    private final EnvelopeManifestFactory manifestFactory;
    private final IndexProviderFactory indexProviderFactory;
    private final boolean disableVerification;
    private final EnvelopeWriter envelopeWriter;
    private final EnvelopeReader envelopeReader;

    private EnvelopePackagingFactory(Builder builder) {
        Util.notNull(builder.signatureFactory, "Signature factory");
        Util.notNull(builder.manifestFactory, "Manifest factory");
        Util.notNull(builder.indexProviderFactory, "Index provider factory");
        Util.notNull(builder.parsingStoreFactory, "Parsing store factory");
        Util.notNull(builder.mimeType, "MIME type");
        Util.notNull(builder.envelopeReader, "Envelope reader");
        Util.notNull(builder.envelopeWriter, "Envelope writer");
        this.signatureFactory = builder.signatureFactory;
        this.manifestFactory = builder.manifestFactory;
        this.indexProviderFactory = builder.indexProviderFactory;
        this.disableVerification = builder.disableInternalVerification;
        this.envelopeMimeType = builder.mimeType;
        this.envelopeWriter = builder.envelopeWriter;
        this.envelopeReader = builder.envelopeReader;
        logger.info("Envelope factory initialized");
    }

    /**
     * Parses an {@link InputStream} to produce a {@link Envelope}.
     *
     * @param inputStream    An {@link InputStream} that contains a valid/parsable {@link Envelope}. This InputStream will be
     *                       closed after reading.
     * @return An instance of {@link Envelope} based on the data from {@link InputStream}. Does not verify
     *         the envelope/signature(s).
     * @throws InvalidPackageException      When the {@link InputStream} does not contain a parsable {@link Envelope}.
     * @throws EnvelopeReadingException    When there were issues parsing some elements of the {@link Envelope}. The parsed
     *         envelope and all encountered exceptions can be retrieved from this exception.
     */
    public Envelope read(InputStream inputStream) throws InvalidPackageException {
        Util.notNull(inputStream, "Input stream");
        try {
            return envelopeReader.read(inputStream);
        } catch (IOException e) {
            throw new InvalidPackageException("Failed to parse InputStream", e);
        } catch (ParsingStoreException e) {
            throw new InvalidPackageException("Failed to create parsing store for envelope data", e);
        }
    }

    /**
     * Provides the MIMETYPE content for envelope.
     */
    public byte[] getMimeTypeContent() {
        return envelopeMimeType.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Creates a {@link Envelope} with the input documents and annotations and a signature covering them.
     *
     * @param files          List of {@link Document} to be added and signed. Can NOT be null.
     * @param annotations    List of {@link Annotation} to be added and signed. Can be null.
     * @return A new {@link Envelope} which contains the documents and annotations and a signature covering them.
     * @throws InvalidPackageException  When the input data can not be processed or signing fails.
     */
    public Envelope create(List<Document> files, List<Annotation> annotations) throws InvalidPackageException {
        SignatureContent signatureContent = verifyAndSign(files, annotations, null);
        Envelope envelope = new Envelope(signatureContent, envelopeWriter);
        verifyEnvelope(envelope);
        return envelope;
    }

    /**
     * Creates a {@link SignatureContent} that contains the new set of
     * documents, annotations and a signature for the added elements and adds it to the {@param existingEnvelope}.
     *
     * @param existingEnvelope    An instance of {@link Envelope} which already has
     *                             {@link EnvelopeSignature}(s)
     * @param files                List of {@link Document} to be added and signed. Can NOT be null.
     * @param annotations          List of {@link Annotation} to be added and signed. Can be null.
     * @throws InvalidPackageException When the input data can not be processed or signing fails.
     * @throws EnvelopeMergingException When there are issues adding the newly created {@link SignatureContent} to {@param existingEnvelope}.
     */
    public void addSignature(Envelope existingEnvelope, List<Document> files, List<Annotation> annotations)
            throws InvalidPackageException, EnvelopeMergingException {
        Util.notNull(existingEnvelope, "Envelope");
        SignatureContent signatureContent = verifyAndSign(files, annotations, existingEnvelope);
        existingEnvelope.add(signatureContent);
        verifyEnvelope(existingEnvelope);
    }

    private SignatureContent verifyAndSign(List<Document> files, List<Annotation> annotations,
                                           Envelope existingEnvelope) throws InvalidPackageException {
        Util.notEmpty(files, "Document files");
        HashSet<Document> documents = new HashSet<>(files);

        IndexProvider indexProvider;
        if (existingEnvelope != null) {
            for (SignatureContent content : existingEnvelope.getSignatureContents()) {
                documents.addAll(content.getDocuments().values());
            }
            indexProvider = indexProviderFactory.create(existingEnvelope);
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

    private void verifyEnvelope(Envelope envelope) throws InvalidPackageException {
        if (disableVerification) {
            return;
        }
        VerifiedEnvelope result = new EnvelopeVerifier(new InternalVerificationPolicy()).verify(envelope);
        if (!result.getVerificationResult().equals(VerificationResult.OK)) {
            try {
                envelope.close();
            } catch (Exception e) {
                logger.warn("Failed to clean up after created envelope that did not pass internal verification.", e);
            }
            for(RuleVerificationResult res : result.getResults()) {
                if(res.getVerificationResult().equals(VerificationResult.NOK)) {
                    logger.error("Failed rule '{}' for '{}' ", res.getRuleName(), res.getTestedElementPath());
                }
                if(res.getVerificationResult().equals(VerificationResult.WARN)) {
                    logger.warn("Failed rule '{}' for '{}' ", res.getRuleName(), res.getTestedElementPath());
                }
            }
            throw new InvalidPackageException("Created envelope did not pass internal verification");
        }
    }

    private void verifyNoDuplicateDocumentNames(Set<Document> documents) throws IllegalArgumentException {
        Set<String> uniqueDocumentNames = new HashSet<>();
        for (Document doc : documents) {
            uniqueDocumentNames.add(doc.getFileName());
        }

        if (uniqueDocumentNames.size() < documents.size()) {
            throw new IllegalArgumentException("Found multiple documents with same name!");
        }
    }

    private static class ContentSigner {

        private List<Document> documents;
        private List<Annotation> annotations;
        private EntryNameProvider nameProvider;
        private EnvelopeManifestFactory manifestFactory;
        private SignatureFactory signatureFactory;

        private List<Pair<String, Annotation>> annotationPairs = new LinkedList<>();
        private List<Pair<String, SingleAnnotationManifest>> singleAnnotationManifestPairs = new LinkedList<>();
        private Map<String, Pair<Annotation, SingleAnnotationManifest>> annotationsManifestContent = new HashMap<>();

        public ContentSigner(List<Document> documents, List<Annotation> annotations, IndexProvider indexProvider,
                             EnvelopeManifestFactory manifestFactory, SignatureFactory signatureFactory) {
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
            logger.info("'{}' is used to create and read envelope manifests", manifestFactoryType.getName());
            logger.info("'{}' is used to create and read envelope signatures", signatureFactoryType.getName());
            Pair<String, DocumentsManifest> documentsManifest =
                    Pair.of(nameProvider.nextDocumentsManifestName(), manifestFactory.createDocumentsManifest(documents));
            processAnnotations(documentsManifest);
            AnnotationsManifest annotationsManifest = manifestFactory.createAnnotationsManifest(annotationsManifestContent);
            Pair<String, AnnotationsManifest> annotationsManifestPair =
                    Pair.of(nameProvider.nextAnnotationsManifestName(), annotationsManifest);

            Manifest manifest = manifestFactory.createManifest(documentsManifest, annotationsManifestPair,
                    Pair.of(nameProvider.nextSignatureName(), signatureFactoryType.getSignatureMimeType()));


            DataHash hash = getSignatureContentSigningHash(manifest);
            EnvelopeSignature signature = signatureFactory.create(hash);
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
            for (Annotation annotation : annotations) {
                Pair<String, Annotation> annotationPair =
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
        protected EnvelopeManifestFactory manifestFactory = new TlvEnvelopeManifestFactory();
        protected boolean disableInternalVerification = false;
        protected IndexProviderFactory indexProviderFactory = new IncrementingIndexProviderFactory();
        protected ParsingStoreFactory parsingStoreFactory = new TemporaryFileBasedParsingStoreFactory();
        protected String mimeType;
        protected EnvelopeWriter envelopeWriter;
        protected EnvelopeReader envelopeReader;

        public Builder withSignatureFactory(SignatureFactory factory) {
            this.signatureFactory = factory;
            return this;
        }

        public Builder withManifestFactory(EnvelopeManifestFactory factory) {
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

        public Builder withEnvelopeWriter(EnvelopeWriter writer) {
            this.envelopeWriter = writer;
            return this;
        }

        public Builder withEnvelopeReader(EnvelopeReader reader) {
            this.envelopeReader = reader;
            return this;
        }

        public Builder enableInternalVerification() {
            this.disableInternalVerification = false;
            return this;
        }

        public EnvelopePackagingFactory build() throws IOException {
            return new EnvelopePackagingFactory(this);
        }
    }

}
