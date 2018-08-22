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

package com.guardtime.envelope.packaging.parsing;

import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.annotation.AnnotationBuilder;
import com.guardtime.envelope.annotation.EnvelopeAnnotationType;
import com.guardtime.envelope.document.Document;
import com.guardtime.envelope.document.DocumentBuilder;
import com.guardtime.envelope.manifest.AnnotationDataReference;
import com.guardtime.envelope.manifest.AnnotationsManifest;
import com.guardtime.envelope.manifest.DocumentsManifest;
import com.guardtime.envelope.manifest.FileReference;
import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.manifest.SingleAnnotationManifest;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.packaging.parsing.handler.ContentParsingException;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.util.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

class SignatureContentComposer {

    private static final Logger logger = LoggerFactory.getLogger(SignatureContentComposer.class);

    private final EnvelopeElementExtractor handler;

    SignatureContentComposer(EnvelopeElementExtractor envelopeElementExtractor) {
        this.handler = envelopeElementExtractor;
    }

    public Pair<SignatureContent, List<Throwable>> compose(String manifestPath, ParsingStoreSession parsingStoreSession)
            throws ContentParsingException {
        SignatureContentGroup group = new SignatureContentGroup(manifestPath, parsingStoreSession);
        SignatureContent signatureContent = new SignatureContent.Builder()
                .withManifest(group.manifest)
                .withDocumentsManifest(group.documentsManifest)
                .withAnnotationsManifest(group.annotationsManifest)
                .withSingleAnnotationManifests(group.singleAnnotationManifests)
                .withDocuments(group.documents)
                .withAnnotations(group.annotations)
                .withSignature(group.signature)
                .build();

        return Pair.of(signatureContent, group.exceptions);
    }

    private class SignatureContentGroup {

        private List<Throwable> exceptions = new ArrayList<>();
        private Manifest manifest;
        private DocumentsManifest documentsManifest;
        private AnnotationsManifest annotationsManifest;
        private List<SingleAnnotationManifest> singleAnnotationManifests = new LinkedList<>();
        private List<Annotation> annotations = new LinkedList<>();
        private List<Document> documents = new LinkedList<>();
        private EnvelopeSignature signature;
        private ParsingStoreSession parsingStoreSession;


        SignatureContentGroup(String manifestPath, ParsingStoreSession storeSession) throws ContentParsingException {
            this.parsingStoreSession = storeSession;
            this.manifest = getManifest(manifestPath);
            this.documentsManifest = getDocumentsManifest();
            this.annotationsManifest = getAnnotationsManifest();

            populateAnnotationsWithManifests();
            populateDocuments();
            fetchSignature();
        }

        private Manifest getManifest(String manifestPath) throws ContentParsingException {
            return handler.getManifest(manifestPath);
        }

        private AnnotationsManifest getAnnotationsManifest() {
            FileReference annotationsManifestReference = manifest.getAnnotationsManifestReference();
            try {
                return handler.getAnnotationsManifest(annotationsManifestReference.getUri());
            } catch (ContentParsingException e) {
                exceptions.add(e);
                return null;
            }
        }

        private DocumentsManifest getDocumentsManifest() {
            FileReference documentsManifestReference = manifest.getDocumentsManifestReference();
            try {
                return handler.getDocumentsManifest(documentsManifestReference.getUri());
            } catch (ContentParsingException e) {
                exceptions.add(e);
                return null;
            }
        }

        private void populateDocuments() {
            if (documentsManifest == null) return;
            for (FileReference reference : documentsManifest.getDocumentReferences()) {
                Document document = fetchDocumentFromHandler(reference);
                if (document != null) documents.add(document);
            }
        }

        private Document fetchDocumentFromHandler(FileReference reference) {
            if (invalidReference(reference)) return null;
            String documentUri = reference.getUri();
            try {
                return new DocumentBuilder()
                        .withDocumentMimeType(reference.getMimeType())
                        .withDocumentName(documentUri)
                        .withParsingStoreReference(handler.getParsingStoreReference(documentUri))
                        .build();
            } catch (ContentParsingException e) {
                // either removed or was never present in the first place, verifier will decide
                return new DocumentBuilder()
                        .withDocumentName(documentUri)
                        .withDocumentMimeType(reference.getMimeType())
                        .withDataHashList(reference.getHashList())
                        .build();
            }
        }

        private boolean invalidReference(FileReference reference) {
            return reference.getUri() == null ||
                    reference.getMimeType() == null ||
                    reference.getHashList() == null ||
                    reference.getHashList().isEmpty();
        }

        private void populateAnnotationsWithManifests() {
            if (annotationsManifest == null) return;
            for (FileReference manifestReference : annotationsManifest.getSingleAnnotationManifestReferences()) {
                SingleAnnotationManifest singleAnnotationManifest = getSingleAnnotationManifest(manifestReference);
                if (singleAnnotationManifest != null) {
                    singleAnnotationManifests.add(singleAnnotationManifest);
                    Annotation annotation = getEnvelopeAnnotation(manifestReference, singleAnnotationManifest);
                    if (annotation != null) annotations.add(annotation);
                }
            }
        }

        private SingleAnnotationManifest getSingleAnnotationManifest(FileReference manifestReference) {
            String manifestUri = manifestReference.getUri();
            try {
                return handler.getSingleAnnotationManifest(manifestUri);
            } catch (ContentParsingException e) {
                logger.debug("Failed to parse manifest for '{}'. Reason: {}", manifestUri, e.getMessage());
                return null;
            }
        }

        private Annotation getEnvelopeAnnotation(FileReference manifestReference,
                                                 SingleAnnotationManifest singleAnnotationManifest) {
            EnvelopeAnnotationType type = EnvelopeAnnotationType.fromContent(manifestReference.getMimeType());
            if (type == null) {
                logger.debug(
                        "Failed to parse annotation for '{}'. Reason: Invalid annotation type: '{}",
                        manifestReference.getUri(),
                        manifestReference.getMimeType()
                );
                return null;
            }
            AnnotationDataReference annotationDataReference = singleAnnotationManifest.getAnnotationReference();
            String uri = annotationDataReference.getUri();
            try {
                Annotation annotation = new AnnotationBuilder()
                        .withParsingStoreReference(parsingStoreSession.get(uri))
                        .withDomain(annotationDataReference.getDomain())
                        .withAnnotationType(type)
                        .withPath(uri)
                        .build();
                return annotation;
            } catch (NullPointerException | IllegalStateException e) {
                logger.debug("Failed to extract Annotation for '{}'.", uri, e);
                return null;
            }
        }

        private void fetchSignature() {
            String signatureUri = manifest.getSignatureReference().getUri();
            try {
                signature = handler.getEnvelopeSignature(signatureUri);
            } catch (ContentParsingException e) {
                exceptions.add(e);
                signature = null;
            }
        }
    }
}
