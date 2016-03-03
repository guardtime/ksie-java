package com.guardtime.container.packaging;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.AnnotationInfoManifest;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.SignatureManifest;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.util.Pair;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.util.List;

public class SignatureContent {

    private final List<ContainerDocument> documents;
    private final Pair<String, DataFilesManifest> dataManifest;
    private final Pair<String, SignatureManifest> manifest;
    private final Pair<String, AnnotationsManifest> annotationsManifest;
    private final List<Pair<String, AnnotationInfoManifest>> annotationManifests;
    private ContainerSignature signature;
    private List<Pair<String, ContainerAnnotation>> annotations;

    private SignatureContent(List<ContainerDocument> documents,
                             List<Pair<String, ContainerAnnotation>> annotations, Pair<String, DataFilesManifest> dataManifest,
                             Pair<String, AnnotationsManifest> annotationsManifest, Pair<String, SignatureManifest> manifest,
                             List<Pair<String, AnnotationInfoManifest>> annotationManifests) {
        this.documents = documents;
        this.annotations = annotations;
        this.dataManifest = dataManifest;
        this.annotationsManifest = annotationsManifest;
        this.manifest = manifest;
        this.annotationManifests = annotationManifests;
    }

    public List<ContainerDocument> getDocuments() {
        return documents;
    }

    public List<Pair<String, ContainerAnnotation>> getAnnotations() {
        return annotations;
    }

    public ContainerSignature getSignature() {
        return signature;
    }

    public void setSignature(ContainerSignature signature) {
        this.signature = signature;
    }

    public DataHash getSignatureInputHash() throws BlockChainContainerException {
        return manifest.getRight().getDataHash(HashAlgorithm.SHA2_256);
    }

    public Pair<String, DataFilesManifest> getDataManifest() {
        return dataManifest;
    }

    public Pair<String, AnnotationsManifest> getAnnotationsManifest() {
        return annotationsManifest;
    }

    public Pair<String, SignatureManifest> getSignatureManifest() {
        return manifest;
    }

    public List<Pair<String, AnnotationInfoManifest>> getAnnotationManifests() {
        return annotationManifests;
    }

    public static class Builder {

        private List<ContainerDocument> documents;
        private List<Pair<String, ContainerAnnotation>> annotations;
        private Pair<String, DataFilesManifest> dataManifest;
        private Pair<String, AnnotationsManifest> annotationsManifest;
        private Pair<String, SignatureManifest> manifest;
        private List<Pair<String, AnnotationInfoManifest>> annotationManifests;

        public Builder withDocuments(List<ContainerDocument> documents) {
            this.documents = documents;
            return this;
        }

        public Builder withAnnotations(List<Pair<String, ContainerAnnotation>> annotations) {
            this.annotations = annotations;
            return this;
        }

        public Builder withDataManifest(Pair<String, DataFilesManifest> dataManifest) {
            this.dataManifest = dataManifest;
            return this;
        }

        public Builder withAnnotationsManifest(Pair<String, AnnotationsManifest> annotationsManifest) {
            this.annotationsManifest = annotationsManifest;
            return this;
        }

        public Builder withManifest(Pair<String, SignatureManifest> manifest) {
            this.manifest = manifest;
            return this;
        }

        public Builder withAnnotationManifests(List<Pair<String, AnnotationInfoManifest>> annotationManifests) {
            this.annotationManifests = annotationManifests;
            return this;
        }

        public SignatureContent build() {
            return new SignatureContent(documents, annotations, dataManifest, annotationsManifest, manifest, annotationManifests);
        }
    }

}
