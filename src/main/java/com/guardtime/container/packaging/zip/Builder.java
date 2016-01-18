package com.guardtime.container.packaging.zip;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDataFile;
import com.guardtime.container.manifest.AnnotationInfoManifest;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.SignatureManifest;

import java.util.LinkedList;
import java.util.List;

public class Builder {

    private DataFilesManifest dataFilesManifest;
    private AnnotationsManifest annotationsManifest;
    private SignatureManifest signatureManifest;

    private List<AnnotationInfoManifest> annotationInfoManifests;

    private List<ContainerDataFile> dataFiles;
    private List<ContainerAnnotation> annotations;

    public Builder(List<ContainerDataFile> dataFiles,List<ContainerAnnotation> annotations) {
        this.annotations = annotations;
        this.dataFiles = dataFiles;
    }

    public Builder withDataFilesManifest(DataFilesManifest dataFilesManifest) {
        this.dataFilesManifest = dataFilesManifest;
        return this;
    }

    public Builder withAnnotationInfoManifests(List<AnnotationInfoManifest> annotationsManifest) {
        this.annotationInfoManifests = (annotationsManifest);
        return this;
    }

    public Builder withAnnotationsManifest(AnnotationsManifest annotationsManifest) {
        this.annotationsManifest = annotationsManifest;
        return this;
    }

    public Builder withSignatureManifest(SignatureManifest signatureManifest) {
        this.signatureManifest = signatureManifest;
        return this;
    }

    public ZipBlockchainContainer build() {
        ZipBlockchainContainer container = new ZipBlockchainContainer(dataFiles, annotations);
        container.setAnnotationsManifest(annotationsManifest);
        container.setDataFilesManifest(dataFilesManifest);
        container.setSignatureManifest(signatureManifest);
        container.setAnnotationInfoManifests(annotationInfoManifests);
        return container;
    }
}