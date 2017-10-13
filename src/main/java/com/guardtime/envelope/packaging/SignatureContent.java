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

import com.guardtime.envelope.annotation.EnvelopeAnnotation;
import com.guardtime.envelope.document.EnvelopeDocument;
import com.guardtime.envelope.document.EmptyEnvelopeDocument;
import com.guardtime.envelope.document.ParsedEnvelopeDocument;
import com.guardtime.envelope.document.StreamEnvelopeDocument;
import com.guardtime.envelope.manifest.AnnotationsManifest;
import com.guardtime.envelope.manifest.DocumentsManifest;
import com.guardtime.envelope.manifest.FileReference;
import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.manifest.SingleAnnotationManifest;
import com.guardtime.envelope.packaging.parsing.store.ParsingStore;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreException;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.util.Pair;
import com.guardtime.ksi.hashing.DataHash;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Structure that groups together all envelope internal structure elements(manifests), documents, annotations and
 * signature that are directly connected to the signature.
 */
public class SignatureContent implements AutoCloseable {

    private final Map<String, EnvelopeDocument> documents;
    private final Pair<String, DocumentsManifest> documentsManifest;
    private final Pair<String, Manifest> manifest;
    private final Pair<String, AnnotationsManifest> annotationsManifest;
    private final Map<String, SingleAnnotationManifest> singleAnnotationManifestMap;
    private final Map<String, EnvelopeAnnotation> annotations;
    private EnvelopeSignature signature;
    private final ParsingStore store;

    protected SignatureContent(Builder builder) {
        this.documents = formatDocumentsListToMap(builder.documents);
        this.annotations = formatAnnotationsListToMap(builder.annotations);
        this.singleAnnotationManifestMap = formatSingleAnnotationManifestsListToMap(builder.singleAnnotationManifests);
        this.documentsManifest = builder.documentsManifest;
        this.annotationsManifest = builder.annotationsManifest;
        this.manifest = builder.manifest;
        this.signature = builder.signature;
        this.store = builder.store;
    }

    /**
     * Provides access to all {@link EnvelopeDocument} contained by the structure.
     *
     * @return Map containing the name and the document.
     */
    public Map<String, EnvelopeDocument> getDocuments() {
        return Collections.unmodifiableMap(documents);
    }

    /**
     * Provides access to all {@link EnvelopeAnnotation} contained by the structure.
     *
     * @return Map containing path and annotation where path is used for envelope management.
     */
    public Map<String, EnvelopeAnnotation> getAnnotations() {
        return Collections.unmodifiableMap(annotations);
    }

    /**
     * Provides access to the {@link EnvelopeSignature} which signs the structure and its content.
     */
    public EnvelopeSignature getEnvelopeSignature() {
        return signature;
    }


    public Pair<String, DocumentsManifest> getDocumentsManifest() {
        return documentsManifest;
    }

    public Pair<String, AnnotationsManifest> getAnnotationsManifest() {
        return annotationsManifest;
    }

    public Pair<String, Manifest> getManifest() {
        return manifest;
    }

    public Map<String, SingleAnnotationManifest> getSingleAnnotationManifests() {
        return Collections.unmodifiableMap(singleAnnotationManifestMap);
    }

    /**
     * Attached data to a detached {@link EnvelopeDocument}. Returns true after successful attachment.
     * @param path Path of the {@link EnvelopeDocument} to attach the data to.
     * @param data Data stream to be attached to the {@link EnvelopeDocument}. NB! Does NOT close the stream!
     */
    public boolean attachDetachedDocument(String path, InputStream data) {
        EnvelopeDocument document = documents.get(path);
        if (document instanceof EmptyEnvelopeDocument) {
            documents.put(path, new StreamEnvelopeDocument(data, document.getMimeType(), document.getFileName()));
            return true;
        }
        return false;
    }

    /**
     * Returns existing {@link EnvelopeDocument} if present and replaces it with an {@link EmptyEnvelopeDocument} in the
     * {@link SignatureContent}. If no document found or if the document is already detached null will be returned.
     * @throws ParsingStoreException When detaching an instance of {@link ParsedEnvelopeDocument} fails.
     */
    public EnvelopeDocument detachDocument(String path) throws ParsingStoreException {
        if (!documents.containsKey(path) || documents.get(path) instanceof EmptyEnvelopeDocument) {
            return null;
        }
        EnvelopeDocument removed = documents.remove(path);
        List<DataHash> removedDocumentHashes = null;
        for (FileReference ref : documentsManifest.getRight().getDocumentReferences()) {
            if (ref.getUri().equals(path)) {
                removedDocumentHashes = ref.getHashList();
                break;
            }
        }
        documents.put(path, new EmptyEnvelopeDocument(removed.getFileName(), removed.getMimeType(), removedDocumentHashes));
        if (removed instanceof ParsedEnvelopeDocument) {
            try (InputStream inputStream = removed.getInputStream()) {
                EnvelopeDocument detached = new StreamEnvelopeDocument(inputStream, removed.getMimeType(), removed.getFileName());
                removed.close();
                return detached;
            } catch (Exception e) {
                throw new ParsingStoreException("Failed to detach document data from envelope data store.", e);
            }
        }
        return removed;
    }

    @Override
    public void close() throws Exception {
        for (EnvelopeAnnotation annotation : annotations.values()) {
            annotation.close();
        }

        for (EnvelopeDocument document : documents.values()) {
            document.close();
        }

        if (store != null) {
            store.close();
        }
    }

    private Map<String, SingleAnnotationManifest> formatSingleAnnotationManifestsListToMap(List<Pair<String, SingleAnnotationManifest>> annotationManifests) {
        Map<String, SingleAnnotationManifest> returnable = new HashMap<>();
        for (Pair<String, SingleAnnotationManifest> manifestPair : annotationManifests) {
            returnable.put(manifestPair.getLeft(), manifestPair.getRight());
        }
        return returnable;
    }

    private Map<String, EnvelopeAnnotation> formatAnnotationsListToMap(List<Pair<String, EnvelopeAnnotation>> annotations) {
        Map<String, EnvelopeAnnotation> returnable = new HashMap<>();
        for (Pair<String, EnvelopeAnnotation> annotation : annotations) {
            returnable.put(annotation.getLeft(), annotation.getRight());
        }
        return returnable;
    }

    private Map<String, EnvelopeDocument> formatDocumentsListToMap(List<EnvelopeDocument> documents) {
        Map<String, EnvelopeDocument> returnable = new HashMap<>();
        for (EnvelopeDocument document : documents) {
            returnable.put(document.getFileName(), document);
        }
        return returnable;
    }

    public static class Builder {

        private List<EnvelopeDocument> documents;
        private List<Pair<String, EnvelopeAnnotation>> annotations;
        private Pair<String, DocumentsManifest> documentsManifest;
        private Pair<String, AnnotationsManifest> annotationsManifest;
        private Pair<String, Manifest> manifest;
        private List<Pair<String, SingleAnnotationManifest>> singleAnnotationManifests;
        private EnvelopeSignature signature;
        private ParsingStore store;

        public Builder withDocuments(Collection<EnvelopeDocument> documents) {
            this.documents = new ArrayList<>(documents);
            return this;
        }

        public Builder withAnnotations(List<Pair<String, EnvelopeAnnotation>> annotations) {
            this.annotations = annotations;
            return this;
        }

        public Builder withDocumentsManifest(Pair<String, DocumentsManifest> documentsManifest) {
            this.documentsManifest = documentsManifest;
            return this;
        }

        public Builder withAnnotationsManifest(Pair<String, AnnotationsManifest> annotationsManifest) {
            this.annotationsManifest = annotationsManifest;
            return this;
        }

        public Builder withManifest(Pair<String, Manifest> manifest) {
            this.manifest = manifest;
            return this;
        }

        public Builder withSingleAnnotationManifests(List<Pair<String, SingleAnnotationManifest>> singleAnnotationManifests) {
            this.singleAnnotationManifests = singleAnnotationManifests;
            return this;
        }

        public Builder withSignature(EnvelopeSignature signature) {
            this.signature = signature;
            return this;
        }

        public Builder withParsingStore(ParsingStore store) {
            this.store = store;
            return this;
        }

        public SignatureContent build() {
            return new SignatureContent(this);
        }
    }

}
