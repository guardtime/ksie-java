/*
 * Copyright 2013-2018 Guardtime, Inc.
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
import com.guardtime.envelope.packaging.exception.InvalidEnvelopeException;
import com.guardtime.envelope.packaging.parsing.EnvelopeReader;
import com.guardtime.envelope.packaging.parsing.store.MemoryBasedParsingStore;
import com.guardtime.envelope.packaging.parsing.store.ParsingStore;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.signature.SignatureException;
import com.guardtime.envelope.signature.SignatureFactory;
import com.guardtime.envelope.signature.SignatureFactoryType;
import com.guardtime.envelope.util.DataHashException;
import com.guardtime.envelope.util.Util;
import com.guardtime.envelope.verification.EnvelopeVerifier;
import com.guardtime.envelope.verification.VerifiedEnvelope;
import com.guardtime.envelope.verification.policy.InternalVerificationPolicy;
import com.guardtime.envelope.verification.policy.VerificationPolicy;
import com.guardtime.envelope.verification.result.RuleVerificationResult;
import com.guardtime.envelope.verification.result.VerificationResult;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.guardtime.envelope.packaging.EntryNameProvider.META_INF;
import static com.guardtime.envelope.packaging.EnvelopeWriter.MIME_TYPE_ENTRY_NAME;

/**
 * Creates or parses {@link Envelope} instances.
 */
public final class EnvelopePackagingFactory {

    private static final Logger logger = LoggerFactory.getLogger(EnvelopePackagingFactory.class);

    private final SignatureFactory signatureFactory;
    private final EnvelopeManifestFactory manifestFactory;
    private final IndexProviderFactory indexProviderFactory;
    private final EnvelopeReader envelopeReader;
    private final VerificationPolicy verificationPolicy;

    private EnvelopePackagingFactory(Builder builder) {
        Util.notNull(builder.signatureFactory, "Signature factory");
        Util.notNull(builder.manifestFactory, "Manifest factory");
        Util.notNull(builder.indexProviderFactory, "Index provider factory");
        Util.notNull(builder.envelopeReader, "Envelope reader");
        this.signatureFactory = builder.signatureFactory;
        this.manifestFactory = builder.manifestFactory;
        this.indexProviderFactory = builder.indexProviderFactory;
        this.verificationPolicy = builder.verificationPolicy;
        this.envelopeReader = builder.envelopeReader;
        logger.info("Envelope factory initialized");
    }

    /**
     * Parses an {@link InputStream} to produce an {@link Envelope}.
     *
     * @param inputStream    an {@link InputStream} that contains a valid/parsable {@link Envelope}. This InputStream will be
     *                       closed after reading.
     *
     * @return An instance of {@link Envelope} based on the data from {@link InputStream}. Does not verify
     *         the envelope/signature(s).
     *
     * @throws InvalidEnvelopeException      when the {@link InputStream} does not contain a parsable {@link Envelope}.
     * @throws EnvelopeReadingException      when there were issues parsing some elements of the {@link Envelope}. The parsed
     *         envelope and all encountered exceptions can be retrieved from this exception.
     */
    public Envelope read(InputStream inputStream) throws InvalidEnvelopeException {
        Util.notNull(inputStream, "Input stream");
        try {
            return envelopeReader.read(inputStream);
        } catch (IOException e) {
            throw new InvalidEnvelopeException("Failed to parse InputStream", e);
        }
    }

    /**
     * Creates a {@link Envelope} with the input documents and annotations and a signature covering them.
     *
     * @param files          list of {@link Document} to be added and signed; can NOT be null.
     * @param annotations    list of {@link Annotation} to be added and signed; can be null.
     *
     * @return A new {@link Envelope} which contains the documents and annotations and a signature covering them.
     *
     * @throws InvalidEnvelopeException when composing the {@link Envelope} fails or its verification fails.
     * @throws SignatureException when acquiring root signature from signing service fails.
     */
    public Envelope create(List<Document> files, List<Annotation> annotations)
            throws InvalidEnvelopeException, SignatureException {
        SignatureContent signatureContent = verifyAndSign(files, annotations, null);
        Envelope envelope = new Envelope(signatureContent);
        verifyEnvelope(envelope);
        return envelope;
    }

    /**
     * Creates a {@link SignatureContent} that contains the new set of
     * documents, annotations and a signature for the added elements and adds it to the existing Envelope.
     *
     * @param existingEnvelope    an instance of {@link Envelope} which already has {@link EnvelopeSignature}(s).
     * @param files                list of {@link Document} to be added and signed; can NOT be null.
     * @param annotations          list of {@link Annotation} to be added and signed; can be null.
     *
     * @throws InvalidEnvelopeException when composing the {@link Envelope} fails or its verification fails.
     * @throws EnvelopeMergingException when there are issues adding the newly created {@link SignatureContent} to
     * existingEnvelope.
     * @throws SignatureException when acquiring root signature from signing service fails.
     */
    public void addSignature(Envelope existingEnvelope, List<Document> files, List<Annotation> annotations)
            throws InvalidEnvelopeException, EnvelopeMergingException, SignatureException {
        Util.notNull(existingEnvelope, "Envelope");
        SignatureContent signatureContent = verifyAndSign(files, annotations, existingEnvelope);
        existingEnvelope.add(signatureContent);
        verifyEnvelope(existingEnvelope);
    }

    private SignatureContent verifyAndSign(List<Document> documentList, List<Annotation> annotations,
                                           Envelope existingEnvelope) throws InvalidEnvelopeException, SignatureException {
        Util.notEmpty(documentList, "Document files");
        validateDocuments(documentList, existingEnvelope);
        IndexProvider indexProvider = getIndexProvider(existingEnvelope);

        try {
            return new ContentSigner(
                    documentList,
                    annotations,
                    indexProvider,
                    manifestFactory,
                    signatureFactory
            ).sign();
        } catch (DataHashException | InvalidManifestException e) {
            throw new InvalidEnvelopeException("Failed to create internal structure!", e);
        }
    }

    private void validateDocuments(List<Document> documentList, Envelope existingEnvelope) {
        Map<String, List<Document>> documentMap = new HashMap<>();
        addToMapWithDuplicateCheck(documentMap, documentList);
        Set<String> allowedDocumentNames = new HashSet<>();
        if (existingEnvelope != null) {
            addToMapWithDuplicateCheck(documentMap, existingEnvelope.getUnknownFiles());
            for (SignatureContent content : existingEnvelope.getSignatureContents()) {
                addToMapWithDuplicateCheck(documentMap, content.getDocuments().values());
                allowedDocumentNames.add(content.getManifest().getSignatureReference().getUri());
                allowedDocumentNames.add(content.getManifest().getPath());
                allowedDocumentNames.add(content.getDocumentsManifest().getPath());
                allowedDocumentNames.add(content.getAnnotationsManifest().getPath());
                allowedDocumentNames.addAll(content.getSingleAnnotationManifests().keySet());
                allowedDocumentNames.addAll(content.getAnnotations().keySet());
            }
        }
        validateDocumentFilenames(documentMap.keySet(), allowedDocumentNames);
    }

    /**
     * Adds content of documents collection to documentMap. Checks for duplicate existance based on filename and datahash.
     */
    private void addToMapWithDuplicateCheck(Map<String, List<Document>> documentMap, Collection<? extends Document> documents) {
        for (Document doc : documents) {
            if (documentMap.containsKey(doc.getFileName())) {
                for (Document other : documentMap.get(doc.getFileName())) {
                    boolean result = false;
                    for (HashAlgorithm algorithm : HashAlgorithm.getImplementedHashAlgorithms()) {
                        // check the algorithm is usable
                        if (algorithm.isDeprecated(new Date())) {
                            continue;
                        }
                        try {
                            if (!doc.getDataHash(algorithm).equals(other.getDataHash(algorithm))) {
                                throw new IllegalArgumentException(
                                        "Found multiple documents with same name and non-matching data hash!"
                                );
                            }
                            result = true;
                        } catch (DataHashException e) {
                            // ignore since EmptyEnvelope or similar
                        }
                    }
                    if (!result) {
                        throw new IllegalArgumentException(
                                "Found multiple documents with same name and no matching data hashes!"
                        );
                    }
                }
                documentMap.get(doc.getFileName()).add(doc);
            } else {
                List<Document> newList = new ArrayList<>();
                newList.add(doc);
                documentMap.put(doc.getFileName(), newList);
            }
        }
    }

    private void validateDocumentFilenames(Collection<String> documentList, Set<String> allowedDocumentNames) {
        for (String filename : documentList) {
            if (allowedDocumentNames.contains(filename)) {
                continue;
            }
            if (filename.equals(META_INF) ||
                    filename.startsWith(META_INF + "/") ||
                    filename.equals(MIME_TYPE_ENTRY_NAME)) {
                throw new IllegalArgumentException("File name is not valid! File name: " + filename);
            }
        }
    }

    private IndexProvider getIndexProvider(Envelope existingEnvelope) {
        if (existingEnvelope != null) {
            return indexProviderFactory.create(existingEnvelope);
        } else {
            return indexProviderFactory.create();
        }
    }

    private void verifyEnvelope(Envelope envelope) throws InvalidEnvelopeException {
        if (this.verificationPolicy == null) {
            return;
        }
        VerifiedEnvelope result = new EnvelopeVerifier(this.verificationPolicy).verify(envelope);
        if (!result.getVerificationResult().equals(VerificationResult.OK)) {
            try {
                envelope.close();
            } catch (Exception e) {
                logger.warn("Failed to clean up after created envelope that did not pass internal verification.", e);
            }
            for (RuleVerificationResult res : result.getResults()) {
                if (res.getVerificationResult().equals(VerificationResult.NOK)) {
                    logger.error("Failed rule '{}' for '{}' ", res.getRuleName(), res.getTestedElementPath());
                }
                if (res.getVerificationResult().equals(VerificationResult.WARN)) {
                    logger.warn("Failed rule '{}' for '{}' ", res.getRuleName(), res.getTestedElementPath());
                }
            }
            throw new InvalidEnvelopeException("Created envelope did not pass internal verification");
        }
    }

    private static class ContentSigner {

        private List<Document> documents;
        private List<Annotation> annotations;
        private EntryNameProvider nameProvider;
        private EnvelopeManifestFactory manifestFactory;
        private SignatureFactory signatureFactory;

        private Map<Annotation, SingleAnnotationManifest> annotationsManifestContent = new HashMap<>();
        private List<SingleAnnotationManifest> singleAnnotationManifests = new ArrayList<>();

        ContentSigner(List<Document> documents, List<Annotation> annotations, IndexProvider indexProvider,
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
            DocumentsManifest documentsManifest =
                    manifestFactory.createDocumentsManifest(documents, nameProvider.nextDocumentsManifestName());
            processAnnotations(documentsManifest);
            AnnotationsManifest annotationsManifest = manifestFactory.createAnnotationsManifest(
                    annotationsManifestContent,
                    nameProvider.nextAnnotationsManifestName()
            );

            Manifest manifest = manifestFactory.createManifest(documentsManifest, annotationsManifest,
                    signatureFactoryType, nameProvider.nextSignatureName(), nameProvider.nextManifestName());


            DataHash hash = getSignatureContentSigningHash(manifest);
            EnvelopeSignature signature = signatureFactory.create(hash);
            SignatureContent signatureContent = new SignatureContent.Builder()
                    .withDocuments(documents)
                    .withDocumentsManifest(documentsManifest)
                    .withAnnotations(annotations)
                    .withSingleAnnotationManifests(singleAnnotationManifests)
                    .withAnnotationsManifest(annotationsManifest)
                    .withManifest(manifest)
                    .withSignature(signature)
                    .build();
            return signatureContent;
        }

        private DataHash getSignatureContentSigningHash(Manifest manifest) throws DataHashException {
            HashAlgorithmProvider algorithmProvider = manifestFactory.getHashAlgorithmProvider();
            return manifest.getDataHash(algorithmProvider.getSigningHashAlgorithm());
        }

        private void processAnnotations(DocumentsManifest documentsManifest) throws InvalidManifestException {
            if (annotations == null) {
                annotations = Collections.emptyList();
                return;
            }
            for (Annotation annotation : annotations) {
                if (annotation.getPath() == null) {
                    annotation.setPath(nameProvider.nextAnnotationDataFileName());
                }
                SingleAnnotationManifest singleAnnotationManifest = manifestFactory.createSingleAnnotationManifest(
                        documentsManifest,
                        annotation,
                        nameProvider.nextSingleAnnotationManifestName()
                );
                singleAnnotationManifests.add(singleAnnotationManifest);
                annotationsManifestContent.put(annotation, singleAnnotationManifest);
            }
        }

    }

    public static class Builder {

        protected SignatureFactory signatureFactory;
        protected EnvelopeManifestFactory manifestFactory = new TlvEnvelopeManifestFactory();
        protected IndexProviderFactory indexProviderFactory = new IncrementingIndexProviderFactory();
        protected ParsingStore parsingStore = MemoryBasedParsingStore.getInstance();
        protected EnvelopeReader envelopeReader;
        protected VerificationPolicy verificationPolicy = new InternalVerificationPolicy();

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

        public Builder withParsingStore(ParsingStore store) {
            this.parsingStore = store;
            return this;
        }

        public Builder withEnvelopeReader(EnvelopeReader reader) {
            this.envelopeReader = reader;
            return this;
        }

        /**
         * Passes provided verification policy to built packaging factory instead of the default
         * {@link InternalVerificationPolicy}.
         * <p>
         * NB! 'null' is valid and disables verification.
         * </p>
         *
         * @param verificationPolicy the verification policy you want to use.
         * @return The same builder.
         */
        public Builder withVerificationPolicy(VerificationPolicy verificationPolicy) {
            this.verificationPolicy = verificationPolicy;
            return this;
        }

        public EnvelopePackagingFactory build() throws IOException {
            return new EnvelopePackagingFactory(this);
        }
    }

}
