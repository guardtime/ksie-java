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

import com.guardtime.envelope.EnvelopeElement;
import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.annotation.AnnotationFactory;
import com.guardtime.envelope.document.Document;
import com.guardtime.envelope.document.DocumentFactory;
import com.guardtime.envelope.document.EmptyDocument;
import com.guardtime.envelope.manifest.AnnotationsManifest;
import com.guardtime.envelope.manifest.DocumentsManifest;
import com.guardtime.envelope.manifest.FileReference;
import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.manifest.SingleAnnotationManifest;
import com.guardtime.envelope.packaging.parsing.store.ParsingStore;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreException;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.util.DataHashException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Structure that groups together all envelope internal structure elements (manifests), documents, annotations and
 * signatures that are directly connected to the signature.
 */
public class SignatureContent implements AutoCloseable, Comparable<SignatureContent> {

    private final Map<String, Document> documents;
    private final DocumentsManifest documentsManifest;
    private final Manifest manifest;
    private final AnnotationsManifest annotationsManifest;
    private final Map<String, SingleAnnotationManifest> singleAnnotationManifestMap;
    private final Map<String, Annotation> annotations;
    private EnvelopeSignature signature;

    protected SignatureContent(Builder builder) {
        this.documents = builder.documents;
        this.annotations = builder.annotations;
        this.singleAnnotationManifestMap = builder.singleAnnotationManifests;
        this.documentsManifest = builder.documentsManifest;
        this.annotationsManifest = builder.annotationsManifest;
        this.manifest = builder.manifest;
        this.signature = builder.signature;
    }

    public SignatureContent(SignatureContent signatureContent, ParsingStore parsingStore) {
        this(new Builder().withSignatureContent(signatureContent, parsingStore));
    }

    private List<Annotation> copyAnnotations(Collection<Annotation> annotations, AnnotationFactory annotationFactory) {
        List<Annotation> copied = new ArrayList<>();
        for (Annotation annot : annotations) {
            copied.add(annotationFactory.create(annot));
        }
        return copied;
    }

    private List<Document> copyDocuments(Collection<Document> documents, DocumentFactory documentFactory) {
        List<Document> copied = new ArrayList<>();
        for (Document doc : documents) {
            copied.add(documentFactory.create(doc));
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
    public boolean attachDetachedDocument(String path, InputStream data, DocumentFactory documentFactory) {
        Document document = documents.get(path);
        if (document instanceof EmptyDocument) {
            documents.put(
                    path,
                    documentFactory.create(data, document.getMimeType(), document.getFileName())
            );
            return true;
        }
        return false;
    }

    /**
     * @param path path of the {@link Document}.
     * @return Existing {@link Document} if present, and replaces it with an {@link EmptyDocument} in the
     * {@link SignatureContent}. If no document found or if the document is already detached, null will be returned.
     * @throws ParsingStoreException when detaching an instance of {@link Document} fails.
     */
    public Document detachDocument(String path, DocumentFactory documentFactory) throws ParsingStoreException {
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
        documents.put(
                path,
                documentFactory.create(removedDocumentHashes, removed.getMimeType(), removed.getFileName())
        );
        try (InputStream inputStream = removed.getInputStream()) {
            Document detached = documentFactory.create(inputStream, removed.getMimeType(), removed.getFileName());
            removed.close();
            return detached;
        } catch (Exception e) {
            throw new ParsingStoreException("Failed to detach document data from data store.", e);
        }
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
                ", documentsManifest= " + (documentsManifest != null ? documentsManifest.getPath() : "null") + '\'' +
                ", annotationsManifest= " + (annotationsManifest != null ? annotationsManifest.getPath() : "null") + '\'' +
                ", documents= '" + documents.keySet() +
                ", singleAnnotationManifests= " + singleAnnotationManifestMap.keySet() +
                ", annotations= " + annotations.keySet() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SignatureContent)) return false;
        SignatureContent content = (SignatureContent) o;
        return getDocuments().equals(content.getDocuments()) &&
                getAnnotations().equals(content.getAnnotations()) &&
                getSingleAnnotationManifests().equals(content.getSingleAnnotationManifests()) &&
                dataHashEquals(getManifest(), content.getManifest()) &&
                dataHashEquals(getDocumentsManifest(), content.getDocumentsManifest()) &&
                dataHashEquals(getAnnotationsManifest(), content.getAnnotationsManifest()) &&
                Objects.equals(getEnvelopeSignature().getSignature(), content.getEnvelopeSignature().getSignature());
    }

    private boolean dataHashEquals(EnvelopeElement thisElement, EnvelopeElement otherElement) {
        try {
            return Objects.equals(
                    thisElement != null ? thisElement.getDataHash(HashAlgorithm.SHA2_256) : null,
                    otherElement != null ? otherElement.getDataHash(HashAlgorithm.SHA2_256) : null
            );
        } catch (DataHashException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int hashCode() {
        try {
            return Objects.hash(
                    getStringDataHashMap(getDocuments()),
                    getDocumentsManifest() != null ? getDocumentsManifest().getDataHash(HashAlgorithm.SHA2_256) : null,
                    getManifest() != null ? getManifest().getDataHash(HashAlgorithm.SHA2_256) : null,
                    getAnnotationsManifest() != null ? getAnnotationsManifest().getDataHash(HashAlgorithm.SHA2_256) : null,
                    getStringDataHashMap(singleAnnotationManifestMap),
                    getStringDataHashMap(getAnnotations()),
                    signature != null ? signature.getSignature() : null
            );
        } catch (DataHashException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, DataHash> getStringDataHashMap(Map<String, ? extends EnvelopeElement> envelopeElementsMap) {
        Map<String, DataHash> resultMap = new HashMap<>();
        for (Map.Entry<String, ? extends EnvelopeElement> entry : envelopeElementsMap.entrySet()) {
            try {
                resultMap.put(entry.getKey(), entry.getValue().getDataHash(HashAlgorithm.SHA2_256));
            } catch (DataHashException e) {
                resultMap.put(entry.getKey(), null);
            }
        }
        return resultMap;
    }

    public static class Builder {

        private Map<String, Document> documents = new HashMap<>();
        private Map<String, Annotation> annotations = new HashMap<>();
        private DocumentsManifest documentsManifest;
        private AnnotationsManifest annotationsManifest;
        private Manifest manifest;
        private Map<String, SingleAnnotationManifest> singleAnnotationManifests = new HashMap<>();
        private EnvelopeSignature signature;

        public Builder() {
        }

        public Builder withDocuments(Collection<Document> documents) {
            for (Document document : documents) {
                this.documents.put(document.getPath(), document);
            }
            return this;
        }

        public Builder withAnnotations(List<Annotation> annotations) {
            for (Annotation annotation : annotations) {
                this.annotations.put(annotation.getPath(), annotation);
            }
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
            for (SingleAnnotationManifest manifest : singleAnnotationManifests) {
                this.singleAnnotationManifests .put(manifest.getPath(), manifest);
            }
            return this;
        }

        public Builder withSignature(EnvelopeSignature signature) {
            this.signature = signature;
            return this;
        }

        protected Builder withSignatureContent(SignatureContent original, ParsingStore parsingStore) {
          return withDocuments(original.copyDocuments(original.getDocuments().values(), new DocumentFactory(parsingStore)))
                  .withAnnotations(
                          original.copyAnnotations(original.getAnnotations().values(), new AnnotationFactory(parsingStore))
                  )
                  .withSignature(original.getEnvelopeSignature().getCopy())
                  // No copy needed for manifests,
                  // can use the same object as they never change during program lifetime.
                  .withSingleAnnotationManifests(new ArrayList<>(original.getSingleAnnotationManifests().values()))
                  .withDocumentsManifest(original.getDocumentsManifest())
                  .withAnnotationsManifest(original.getAnnotationsManifest())
                  .withManifest(original.getManifest());
        }

        public SignatureContent build() {
            return new SignatureContent(this);
        }
    }

}
