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
import com.guardtime.envelope.document.EmptyDocument;
import com.guardtime.envelope.document.ParsedDocument;
import com.guardtime.envelope.document.StreamDocument;
import com.guardtime.envelope.manifest.AnnotationsManifest;
import com.guardtime.envelope.manifest.DocumentsManifest;
import com.guardtime.envelope.manifest.FileReference;
import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.manifest.SingleAnnotationManifest;
import com.guardtime.envelope.packaging.parsing.store.ParsingStore;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreException;
import com.guardtime.envelope.signature.EnvelopeSignature;
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
public class SignatureContent implements AutoCloseable, Comparable<SignatureContent> {

    private final Map<String, Document> documents;
    private final DocumentsManifest documentsManifest;
    private final Manifest manifest;
    private final AnnotationsManifest annotationsManifest;
    private final Map<String, SingleAnnotationManifest> singleAnnotationManifestMap;
    private final Map<String, Annotation> annotations;
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
     * Provides access to all {@link Document} contained by the structure.
     *
     * @return Map containing the name and the document.
     */
    public Map<String, Document> getDocuments() {
        return Collections.unmodifiableMap(documents);
    }

    /**
     * Provides access to all {@link Annotation} contained by the structure.
     *
     * @return Map containing path and annotation where path is used for envelope management.
     */
    public Map<String, Annotation> getAnnotations() {
        return Collections.unmodifiableMap(annotations);
    }

    /**
     * Provides access to the {@link EnvelopeSignature} which signs the structure and its content.
     */
    public EnvelopeSignature getEnvelopeSignature() {
        return signature;
    }


    public DocumentsManifest getDocumentsManifest() {
        return documentsManifest;
    }

    public AnnotationsManifest getAnnotationsManifest() {
        return annotationsManifest;
    }

    public Manifest getManifest() {
        return manifest;
    }

    public Map<String, SingleAnnotationManifest> getSingleAnnotationManifests() {
        return Collections.unmodifiableMap(singleAnnotationManifestMap);
    }

    /**
     * Attached data to a detached {@link Document}. Returns true after successful attachment.
     * @param path Path of the {@link Document} to attach the data to.
     * @param data Data stream to be attached to the {@link Document}. NB! Does NOT close the stream!
     */
    public boolean attachDetachedDocument(String path, InputStream data) {
        Document document = documents.get(path);
        if (document instanceof EmptyDocument) {
            documents.put(path, new StreamDocument(data, document.getMimeType(), document.getFileName()));
            return true;
        }
        return false;
    }

    /**
     * Returns existing {@link Document} if present and replaces it with an {@link EmptyDocument} in the
     * {@link SignatureContent}. If no document found or if the document is already detached null will be returned.
     * @throws ParsingStoreException When detaching an instance of {@link ParsedDocument} fails.
     */
    public Document detachDocument(String path) throws ParsingStoreException {
        if (!documents.containsKey(path) || documents.get(path) instanceof EmptyDocument) {
            return null;
        }
        Document removed = documents.remove(path);
        List<DataHash> removedDocumentHashes = null;
        for (FileReference ref : documentsManifest.getDocumentReferences()) {
            if (ref.getUri().equals(path)) {
                removedDocumentHashes = ref.getHashList();
                break;
            }
        }
        documents.put(path, new EmptyDocument(removed.getFileName(), removed.getMimeType(), removedDocumentHashes));
        if (removed instanceof ParsedDocument) {
            try (InputStream inputStream = removed.getInputStream()) {
                Document detached = new StreamDocument(inputStream, removed.getMimeType(), removed.getFileName());
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
        for (Annotation annotation : annotations.values()) {
            annotation.close();
        }

        for (Document document : documents.values()) {
            document.close();
        }

        if (store != null) {
            store.close();
        }
    }

    private Map<String, SingleAnnotationManifest> formatSingleAnnotationManifestsListToMap(
            List<SingleAnnotationManifest> annotationManifests) {
        Map<String, SingleAnnotationManifest> returnable = new HashMap<>();
        for (SingleAnnotationManifest manifest : annotationManifests) {
            returnable.put(manifest.getPath(), manifest);
        }
        return returnable;
    }

    private Map<String, Annotation> formatAnnotationsListToMap(List<Annotation> annotations) {
        Map<String, Annotation> returnable = new HashMap<>();
        for (Annotation annotation : annotations) {
            returnable.put(annotation.getPath(), annotation);
        }
        return returnable;
    }

    private Map<String, Document> formatDocumentsListToMap(List<Document> documents) {
        Map<String, Document> returnable = new HashMap<>();
        for (Document document : documents) {
            returnable.put(document.getPath(), document);
        }
        return returnable;
    }

    @Override
    public int compareTo(SignatureContent other) {
        if (other == null || other.getEnvelopeSignature() == null) {
            if (this.signature == null) {
                return 0;
            }
            // The other content is unsigned, moves to the back of any sorting.
            return -1;
        }
        if (this.signature == null) {
            return 1;
        }
        int compared = this.signature.compareTo(other.getEnvelopeSignature());
        if (compared == 0) {
            String signatureUri = this.getManifest().getSignatureReference().getUri();
            String otherSignatureUri = other.getManifest().getSignatureReference().getUri();
            return signatureUri.compareTo(otherSignatureUri);
        }
        return compared;
    }

    public static class Builder {

        private List<Document> documents;
        private List<Annotation> annotations;
        private DocumentsManifest documentsManifest;
        private AnnotationsManifest annotationsManifest;
        private Manifest manifest;
        private List<SingleAnnotationManifest> singleAnnotationManifests;
        private EnvelopeSignature signature;
        private ParsingStore store;

        public Builder withDocuments(Collection<Document> documents) {
            this.documents = new ArrayList<>(documents);
            return this;
        }

        public Builder withAnnotations(List<Annotation> annotations) {
            this.annotations = annotations;
            return this;
        }

        public Builder withDocumentsManifest(DocumentsManifest documentsManifest) {
            this.documentsManifest = documentsManifest;
            return this;
        }

        public Builder withAnnotationsManifest(AnnotationsManifest annotationsManifest) {
            this.annotationsManifest = annotationsManifest;
            return this;
        }

        public Builder withManifest(Manifest manifest) {
            this.manifest = manifest;
            return this;
        }

        public Builder withSingleAnnotationManifests(List<SingleAnnotationManifest> singleAnnotationManifests) {
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
