package com.guardtime.container.packaging;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.EmptyContainerDocument;
import com.guardtime.container.document.StreamContainerDocument;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.util.Pair;
import com.guardtime.ksi.hashing.DataHash;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Structure that groups together all container internal structure elements(manifests), documents, annotations and
 * signature that are directly connected to the signature.
 */
public class SignatureContent {

    private final Map<String, ContainerDocument> documents;
    private final Pair<String, DocumentsManifest> documentsManifest;
    private final Pair<String, Manifest> manifest;
    private final Pair<String, AnnotationsManifest> annotationsManifest;
    private final Map<String, SingleAnnotationManifest> singleAnnotationManifestMap;
    private final Map<String, ContainerAnnotation> annotations;
    private ContainerSignature signature;

    protected SignatureContent(Builder builder) {
        this.documents = formatDocumentsListToMap(builder.documents);
        this.annotations = formatAnnotationsListToMap(builder.annotations);
        this.singleAnnotationManifestMap = formatSingleAnnotationManifestsListToMap(builder.singleAnnotationManifests);
        this.documentsManifest = builder.documentsManifest;
        this.annotationsManifest = builder.annotationsManifest;
        this.manifest = builder.manifest;
        this.signature = builder.signature;
    }

    /**
     * Provides access to all {@link ContainerDocument} contained by the structure.
     *
     * @return Map containing the name and the document.
     */
    public Map<String, ContainerDocument> getDocuments() {
        return Collections.unmodifiableMap(documents);
    }

    /**
     * Provides access to all {@link ContainerAnnotation} contained by the structure.
     *
     * @return Map containing path and annotation where path is used for container management.
     */
    public Map<String, ContainerAnnotation> getAnnotations() {
        return Collections.unmodifiableMap(annotations);
    }

    /**
     * Provides access to the {@link ContainerSignature} which signs the structure and its content.
     */
    public ContainerSignature getContainerSignature() {
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
     * Attached data to a detached {@link ContainerDocument}. Returns true after successful attachment.
     * @param path Path of the {@link ContainerDocument} to attach the data to.
     * @param data Data stream to be attached to the {@link ContainerDocument}. NB! Does NOT close the stream!
     */
    public boolean attachDetachedDocument(String path, InputStream data) {
        ContainerDocument document = documents.get(path);
        if (document != null && document instanceof EmptyContainerDocument) {
            documents.put(path, new StreamContainerDocument(data, document.getMimeType(), document.getFileName()));
            return true;
        }
        return false;
    }

    /**
     * Returns existing {@link ContainerDocument} if present and replaces it with an {@link EmptyContainerDocument} in the
     * {@link SignatureContent}. If no document found or if the document is already detached null will be returned.
     */
    public ContainerDocument detachDocument(String path) {
        if (!documents.containsKey(path) || documents.get(path) instanceof EmptyContainerDocument) {
            return null;
        }
        ContainerDocument removed = documents.remove(path);
        List<DataHash> removedDocumentHashes = new LinkedList<>();
        for (FileReference ref : documentsManifest.getRight().getDocumentReferences()) {
            if (ref.getUri().equals(path)) {
                removedDocumentHashes.addAll(ref.getHashList());
                break;
            }
        }
        documents.put(path, new EmptyContainerDocument(removed.getFileName(), removed.getMimeType(), removedDocumentHashes));
        return removed;
    }

    private Map<String, SingleAnnotationManifest> formatSingleAnnotationManifestsListToMap(List<Pair<String, SingleAnnotationManifest>> annotationManifests) {
        Map<String, SingleAnnotationManifest> returnable = new HashMap<>();
        for (Pair<String, SingleAnnotationManifest> manifestPair : annotationManifests) {
            returnable.put(manifestPair.getLeft(), manifestPair.getRight());
        }
        return returnable;
    }

    private Map<String, ContainerAnnotation> formatAnnotationsListToMap(List<Pair<String, ContainerAnnotation>> annotations) {
        Map<String, ContainerAnnotation> returnable = new HashMap<>();
        for (Pair<String, ContainerAnnotation> annotation : annotations) {
            returnable.put(annotation.getLeft(), annotation.getRight());
        }
        return returnable;
    }

    private Map<String, ContainerDocument> formatDocumentsListToMap(List<ContainerDocument> documents) {
        Map<String, ContainerDocument> returnable = new HashMap<>();
        for (ContainerDocument document : documents) {
            returnable.put(document.getFileName(), document);
        }
        return returnable;
    }

    public static class Builder {

        private List<ContainerDocument> documents;
        private List<Pair<String, ContainerAnnotation>> annotations;
        private Pair<String, DocumentsManifest> documentsManifest;
        private Pair<String, AnnotationsManifest> annotationsManifest;
        private Pair<String, Manifest> manifest;
        private List<Pair<String, SingleAnnotationManifest>> singleAnnotationManifests;
        private ContainerSignature signature;

        public Builder withDocuments(List<ContainerDocument> documents) {
            this.documents = documents;
            return this;
        }

        public Builder withAnnotations(List<Pair<String, ContainerAnnotation>> annotations) {
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

        public Builder withSignature(ContainerSignature signature) {
            this.signature = signature;
            return this;
        }

        public SignatureContent build() {
            return new SignatureContent(this);
        }
    }

}
