package com.guardtime.container.packaging;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.util.Pair;

import java.util.HashMap;
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
    private Map<String, ContainerAnnotation> annotations;
    protected ContainerSignature signature;

    protected SignatureContent(List<ContainerDocument> documents,
                               List<Pair<String, ContainerAnnotation>> annotations, Pair<String, DocumentsManifest> documentsManifest,
                               Pair<String, AnnotationsManifest> annotationsManifest, Pair<String, Manifest> manifest,
                               List<Pair<String, SingleAnnotationManifest>> singleAnnotationManifestMap) {
        this.documents = formatDocumentsListToMap(documents);
        this.annotations = formatAnnotationsListToMap(annotations);
        this.singleAnnotationManifestMap = formatSingleAnnotationManifestsListToMap(singleAnnotationManifestMap);
        this.documentsManifest = documentsManifest;
        this.annotationsManifest = annotationsManifest;
        this.manifest = manifest;
    }

    /**
     * Provides access to all {@link ContainerDocument} contained by the structure.
     *
     * @return Map containing the name and the document.
     */
    public Map<String, ContainerDocument> getDocuments() {
        return documents;
    }

    /**
     * Provides access to all {@link ContainerAnnotation} contained by the structure.
     *
     * @return Map containing path and annotation where path is used for container management.
     */
    public Map<String, ContainerAnnotation> getAnnotations() {
        return annotations;
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
        return singleAnnotationManifestMap;
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

        protected List<ContainerDocument> documents;
        protected List<Pair<String, ContainerAnnotation>> annotations;
        protected Pair<String, DocumentsManifest> documentsManifest;
        protected Pair<String, AnnotationsManifest> annotationsManifest;
        protected Pair<String, Manifest> manifest;
        protected List<Pair<String, SingleAnnotationManifest>> singleAnnotationManifests;

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

        public SignatureContent build() {
            return new SignatureContent(documents, annotations, documentsManifest, annotationsManifest, manifest, singleAnnotationManifests);
        }
    }

}
