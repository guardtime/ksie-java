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

package com.guardtime.envelope.packaging.parsing;

import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.annotation.EnvelopeAnnotationType;
import com.guardtime.envelope.annotation.ParsedAnnotation;
import com.guardtime.envelope.document.Document;
import com.guardtime.envelope.document.EmptyDocument;
import com.guardtime.envelope.document.ParsedDocument;
import com.guardtime.envelope.manifest.AnnotationDataReference;
import com.guardtime.envelope.manifest.AnnotationsManifest;
import com.guardtime.envelope.manifest.DocumentsManifest;
import com.guardtime.envelope.manifest.FileReference;
import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.manifest.SingleAnnotationManifest;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.packaging.parsing.handler.AnnotationContentHandler;
import com.guardtime.envelope.packaging.parsing.handler.AnnotationsManifestHandler;
import com.guardtime.envelope.packaging.parsing.handler.ContentParsingException;
import com.guardtime.envelope.packaging.parsing.handler.DocumentContentHandler;
import com.guardtime.envelope.packaging.parsing.handler.DocumentsManifestHandler;
import com.guardtime.envelope.packaging.parsing.handler.ManifestHandler;
import com.guardtime.envelope.packaging.parsing.handler.SignatureHandler;
import com.guardtime.envelope.packaging.parsing.handler.SingleAnnotationManifestHandler;
import com.guardtime.envelope.packaging.parsing.store.ParsingStore;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreException;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreFactory;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SignatureContentHandler {

    private final DocumentContentHandler documentHandler;
    private final AnnotationContentHandler annotationContentHandler;
    private final ManifestHandler manifestHandler;
    private final DocumentsManifestHandler documentsManifestHandler;
    private final AnnotationsManifestHandler annotationsManifestHandler;
    private final SingleAnnotationManifestHandler singleAnnotationManifestHandler;
    private final SignatureHandler signatureHandler;

    public SignatureContentHandler(DocumentContentHandler documentHandler, AnnotationContentHandler annotationContentHandler,
                                   ManifestHandler manifestHandler, DocumentsManifestHandler documentsManifestHandler,
                                   AnnotationsManifestHandler annotationsManifestHandler, SingleAnnotationManifestHandler singleAnnotationManifestHandler,
                                   SignatureHandler signatureHandler) {
        this.documentHandler = documentHandler;
        this.annotationContentHandler = annotationContentHandler;
        this.manifestHandler = manifestHandler;
        this.documentsManifestHandler = documentsManifestHandler;
        this.annotationsManifestHandler = annotationsManifestHandler;
        this.singleAnnotationManifestHandler = singleAnnotationManifestHandler;
        this.signatureHandler = signatureHandler;
    }

    public Pair<SignatureContent, List<Throwable>> get(String manifestPath, ParsingStoreFactory parsingStoreFactory)
            throws ContentParsingException, ParsingStoreException {
        SignatureContentGroup group = new SignatureContentGroup(manifestPath, parsingStoreFactory.create());
        SignatureContent signatureContent = new SignatureContent.Builder()
                .withManifest(group.manifest)
                .withDocumentsManifest(group.documentsManifest)
                .withAnnotationsManifest(group.annotationsManifest)
                .withSingleAnnotationManifests(group.singleAnnotationManifests)
                .withDocuments(group.documents)
                .withAnnotations(group.annotations)
                .withSignature(group.signature)
                .withParsingStore(group.parsingStore)
                .build();

        return Pair.of(signatureContent, group.exceptions);
    }

    private class SignatureContentGroup {

        List<Throwable> exceptions = new ArrayList<>();
        Pair<String, Manifest> manifest;
        Pair<String, DocumentsManifest> documentsManifest;
        Pair<String, AnnotationsManifest> annotationsManifest;
        List<Pair<String, SingleAnnotationManifest>> singleAnnotationManifests = new LinkedList<>();
        List<Pair<String, Annotation>> annotations = new LinkedList<>();
        List<Document> documents = new LinkedList<>();
        EnvelopeSignature signature;
        ParsingStore parsingStore;


        public SignatureContentGroup(String manifestPath, ParsingStore parsingStore) throws ContentParsingException {
            this.parsingStore = parsingStore;
            this.manifest = getManifest(manifestPath);
            this.documentsManifest = getDocumentsManifest();
            this.annotationsManifest = getAnnotationsManifest();

            populateAnnotationsWithManifests();
            populateDocuments();
            fetchSignature();
        }

        private Pair<String, Manifest> getManifest(String manifestPath) throws ContentParsingException {
            return Pair.of(manifestPath, manifestHandler.get(manifestPath));
        }

        private Pair<String, AnnotationsManifest> getAnnotationsManifest() {
            FileReference annotationsManifestReference = manifest.getRight().getAnnotationsManifestReference();
            try {
                return Pair.of(
                        annotationsManifestReference.getUri(),
                        annotationsManifestHandler.get(annotationsManifestReference.getUri())
                );
            } catch (ContentParsingException e) {
                exceptions.add(e);
                return null;
            }
        }

        private Pair<String, DocumentsManifest> getDocumentsManifest() {
            FileReference documentsManifestReference = manifest.getRight().getDocumentsManifestReference();
            try {
                return Pair.of(documentsManifestReference.getUri(), documentsManifestHandler.get(documentsManifestReference.getUri()));
            } catch (ContentParsingException e) {
                exceptions.add(e);
                return null;
            }
        }

        private void populateDocuments() {
            if (documentsManifest == null) return;
            for (FileReference reference : documentsManifest.getRight().getDocumentReferences()) {
                Document document = fetchDocumentFromHandler(reference);
                if (document != null) documents.add(document);
            }
        }

        private Document fetchDocumentFromHandler(FileReference reference) {
            if (invalidReference(reference)) return null;
            String documentUri = reference.getUri();
            try (InputStream stream = documentHandler.get(documentUri)) {
                parsingStore.store(documentUri, stream);
                return new ParsedDocument(parsingStore, documentUri, reference.getMimeType(), documentUri);
            } catch (ContentParsingException | ParsingStoreException | IOException e) {
                // either removed or was never present in the first place, verifier will decide
                return new EmptyDocument(documentUri, reference.getMimeType(), reference.getHashList());
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
            for (FileReference manifestReference : annotationsManifest.getRight().getSingleAnnotationManifestReferences()) {
                Pair<String, SingleAnnotationManifest> singleAnnotationManifest = getSingleAnnotationManifest(manifestReference);
                if (singleAnnotationManifest != null) {
                    singleAnnotationManifests.add(singleAnnotationManifest);
                    Pair<String, Annotation> annotation = getEnvelopeAnnotation(manifestReference, singleAnnotationManifest.getRight());
                    if (annotation != null) annotations.add(annotation);
                }
            }
        }

        private Pair<String, SingleAnnotationManifest> getSingleAnnotationManifest(FileReference manifestReference) {
            try {
                String manifestReferenceUri = manifestReference.getUri();
                SingleAnnotationManifest singleAnnotationManifest = singleAnnotationManifestHandler.get(manifestReferenceUri);
                return Pair.of(manifestReferenceUri, singleAnnotationManifest);
            } catch (ContentParsingException e) {
                exceptions.add(e);
                return null;
            }
        }

        private Pair<String, Annotation> getEnvelopeAnnotation(FileReference manifestReference, SingleAnnotationManifest singleAnnotationManifest) {
            EnvelopeAnnotationType type = EnvelopeAnnotationType.fromContent(manifestReference.getMimeType());
            if (type == null) {
                String message = String.format(
                        "Failed to parse annotation for '%s'. Reason: Invalid annotation type: '%s'",
                        manifestReference.getUri(),
                        manifestReference.getMimeType()
                );
                exceptions.add(new ContentParsingException(message));
                return null;
            }
            AnnotationDataReference annotationDataReference = singleAnnotationManifest.getAnnotationReference();
            String uri = annotationDataReference.getUri();
            try (InputStream stream = annotationContentHandler.get(uri)) {
                parsingStore.store(uri, stream);
                Annotation annotation = new ParsedAnnotation(parsingStore, uri, annotationDataReference.getDomain(), type);
                return Pair.of(uri, annotation);
            } catch (ContentParsingException | ParsingStoreException | IOException e) {
                exceptions.add(e);
                return null;
            }
        }

        private void fetchSignature() {
            String signatureUri = manifest.getRight().getSignatureReference().getUri();
            try {
                signature = signatureHandler.get(signatureUri);
            } catch (ContentParsingException e) {
                exceptions.add(e);
                signature = null;
            }
        }
    }
}