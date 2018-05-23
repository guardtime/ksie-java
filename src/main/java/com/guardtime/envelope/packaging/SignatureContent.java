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
import com.guardtime.envelope.document.EmptyDocument;
import com.guardtime.envelope.document.ParsedDocument;
import com.guardtime.envelope.manifest.AnnotationsManifest;
import com.guardtime.envelope.manifest.DocumentsManifest;
import com.guardtime.envelope.manifest.FileReference;
import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.manifest.SingleAnnotationManifest;
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
 * Structure that groups together all envelope internal structure elements (manifests), documents, annotations and
 * signatures that are directly connected to the signature.
 */
public class SignatureContent implements AutoCloseable, Comparable<SignatureContent>, Cloneable {

    private final Map<String, Document> documents;
    private final DocumentsManifest documentsManifest;
    private final Manifest manifest;
    private final AnnotationsManifest annotationsManifest;
    private final Map<String, SingleAnnotationManifest> singleAnnotationManifestMap;
    private final Map<String, Annotation> annotations;
    private EnvelopeSignature signature;

    protected SignatureContent(Builder builder) {
        this.documents = formatDocumentsListToMap(builder.documents);
        this.annotations = formatAnnotationsListToMap(builder.annotations);
        this.singleAnnotationManifestMap = formatSingleAnnotationManifestsListToMap(builder.singleAnnotationManifests);
        this.documentsManifest = builder.documentsManifest;
        this.annotationsManifest = builder.annotationsManifest;
        this.manifest = builder.manifest;
        this.signature = builder.signature;
    }

    @Override
    public SignatureContent clone() {
        Builder copyBuilder = new Builder();
        copyBuilder.withDocuments(copyDocuments(getDocuments().values()))
                .withAnnotations(copyAnnotations(getAnnotations().values()))
                .withSingleAnnotationManifests(new ArrayList<>(getSingleAnnotationManifests().values()))
        // No copy needed for manifests and signature, can use the same object as they never change during program lifetime.
                .withDocumentsManifest(getDocumentsManifest())
                .withAnnotationsManifest(getAnnotationsManifest())
                .withManifest(getManifest())
                .withSignature(getEnvelopeSignature());
        return new SignatureContent(copyBuilder);
    }

    private List<Annotation> copyAnnotations(Collection<Annotation> annotations) {
        List<Annotation> copied = new ArrayList<>();
        for (Annotation annot : annotations) {
            copied.add(annot.clone());
        }
        return copied;
    }

    private List<Document> copyDocuments(Collection<Document> documents) {
        List<Document> copied = new ArrayList<>();
        for (Document doc : documents) {
            copied.add(doc.clone());
        }
        return copied;
    }

    /**
     * @return Map containing the {@link Document} contained by the structure, and the document's name.
     */
    public Map<String, Document> getDocuments() {
        return Collections.unmodifiableMap(documents);
    }

    /**
     * @return Map containing the {@link Annotation}s contained by the structure, and the paths to the annotation.
     * Path is used for envelope management.
     */
    public Map<String, Annotation> getAnnotations() {
        return Collections.unmodifiableMap(annotations);
    }

    /**
     * @return The {@link EnvelopeSignature} which signs the structure and its contents.
     */
    public EnvelopeSignature getEnvelopeSignature() {
        return signature;
    }

    /**
     * @return The {@link DocumentsManifest} contained by the structure.
     */
    public DocumentsManifest getDocumentsManifest() {
        return documentsManifest;
    }

    /**
     * @return The {@link AnnotationsManifest} contained by the structure.
     */
    public AnnotationsManifest getAnnotationsManifest() {
        return annotationsManifest;
    }

    /**
     * @return The structure's {@link Manifest}.
     */
    public Manifest getManifest() {
        return manifest;
    }

    public Map<String, SingleAnnotationManifest> getSingleAnnotationManifests() {
        return Collections.unmodifiableMap(singleAnnotationManifestMap);
    }

    /**
     * Attaches data to a detached {@link Document}.
     * @param path path of the {@link Document} to attach the data to.
     * @param data data stream to be attached to the {@link Document}. NB! Does NOT close the stream!
     * @return True, after successful attachment.
     */
    public boolean attachDetachedDocument(String path, InputStream data) {
        Document document = documents.get(path);
        if (document instanceof EmptyDocument) {
            documents.put(path, new ParsedDocument(data, document.getMimeType(), document.getFileName()));
            return true;
        }
        return false;
    }

    /**
     * @param path path of the {@link Document}.
     * @return Existing {@link Document} if present, and replaces it with an {@link EmptyDocument} in the
     * {@link SignatureContent}. If no document found or if the document is already detached, null will be returned.
     * @throws ParsingStoreException when detaching an instance of {@link ParsedDocument} fails.
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
                Document detached = new ParsedDocument(inputStream, removed.getMimeType(), removed.getFileName());
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

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " {" +
                "manifest= " + manifest.getPath() + '\'' +
                ", signature= " + manifest.getSignatureReference().getUri() +
                ", documentsManifest= " + documentsManifest != null ? documentsManifest.getPath() : "null" + '\'' +
                ", annotationsManifest= " + annotationsManifest != null ? annotationsManifest.getPath() : "null" + '\'' +
                ", documents= '" + documents.keySet() +
                ", singleAnnotationManifests= " + singleAnnotationManifestMap.keySet() +
                ", annotations= " + annotations.keySet() +
                '}';
    }

    public static class Builder {

        private List<Document> documents;
        private List<Annotation> annotations;
        private DocumentsManifest documentsManifest;
        private AnnotationsManifest annotationsManifest;
        private Manifest manifest;
        private List<SingleAnnotationManifest> singleAnnotationManifests;
        private EnvelopeSignature signature;

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

        public SignatureContent build() {
            return new SignatureContent(this);
        }
    }

}
