package com.guardtime.container.packaging.zip;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.AnnotationInfoManifest;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.SignatureManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.util.Pair;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class ZipSignatureContent implements SignatureContent {

    private final Map<String, ContainerDocument> documents;
    private final Pair<String, DataFilesManifest> dataManifest;
    private final Pair<String, SignatureManifest> manifest;
    private final Pair<String, AnnotationsManifest> annotationsManifest;
    private final Map<String, AnnotationInfoManifest> annotationInfoManifests;
    private ContainerSignature signature;
    private Map<String, ContainerAnnotation> annotations;

    private ZipSignatureContent(List<ContainerDocument> documents,
                                List<Pair<String, ContainerAnnotation>> annotations, Pair<String, DataFilesManifest> dataManifest,
                                Pair<String, AnnotationsManifest> annotationsManifest, Pair<String, SignatureManifest> manifest,
                                List<Pair<String, AnnotationInfoManifest>> annotationInfoManifests) {
        this.documents = formatDocumentsListToMap(documents);
        this.annotations = formatAnnotationsListToMap(annotations);
        this.annotationInfoManifests = formatAnnotationInfoManifestsListToMap(annotationInfoManifests);
        this.dataManifest = dataManifest;
        this.annotationsManifest = annotationsManifest;
        this.manifest = manifest;
    }

    private Map<String, AnnotationInfoManifest> formatAnnotationInfoManifestsListToMap(List<Pair<String, AnnotationInfoManifest>> annotationManifests) {
        Map<String, AnnotationInfoManifest> returnable = new HashMap<>();
        for (Pair<String, AnnotationInfoManifest> manifestPair : annotationManifests) {
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

    public Map<String, ContainerDocument> getDocuments() {
        return documents;
    }

    public Map<String, ContainerAnnotation> getAnnotations() {
        return annotations;
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

    public Map<String, AnnotationInfoManifest> getAnnotationInfoManifests() {
        return annotationInfoManifests;
    }

    public ContainerSignature getSignature() {
        return signature;
    }

    public void setSignature(ContainerSignature signature) {
        this.signature = signature;
    }

    public DataHash getSignatureInputHash() throws IOException {
        return manifest.getRight().getDataHash(HashAlgorithm.SHA2_256);
    }

    public void writeTo(ZipOutputStream output) throws IOException {
        writeDocuments(output);
        writeEntry(new ZipEntry(dataManifest.getLeft()), dataManifest.getRight().getInputStream(), output);
        writeAnnotations(output);
        writeAnnotationInfoManifests(output);
        writeEntry(new ZipEntry(annotationsManifest.getLeft()), annotationsManifest.getRight().getInputStream(), output);
        writeEntry(new ZipEntry(manifest.getLeft()), manifest.getRight().getInputStream(), output);
        writeSignature(output);
    }

    private void writeAnnotationInfoManifests(ZipOutputStream output) throws IOException {
        for (String uri : annotationInfoManifests.keySet()) {
            AnnotationInfoManifest manifest = annotationInfoManifests.get(uri);
            writeEntry(new ZipEntry(uri), manifest.getInputStream(), output);
        }
    }

    private void writeAnnotations(ZipOutputStream output) throws IOException {
        for (String uri : annotations.keySet()) {
            ContainerAnnotation annotation = annotations.get(uri);
            writeEntry(new ZipEntry(uri), annotation.getInputStream(), output);
        }
    }

    private void writeSignature(ZipOutputStream output) throws IOException {
        String signatureUri = manifest.getRight().getSignatureReference().getUri();
        ZipEntry signatureEntry = new ZipEntry(signatureUri);
        output.putNextEntry(signatureEntry);
        signature.writeTo(output);
        output.closeEntry();
    }

    private void writeDocuments(ZipOutputStream zipOutputStream) throws IOException {
        for (String uri : documents.keySet()) {
            ContainerDocument dataFile = documents.get(uri);
            if (dataFile.isWritable()) {
                writeEntry(new ZipEntry(uri), dataFile.getInputStream(), zipOutputStream);
            }
        }
    }

    private void writeEntry(ZipEntry entry, InputStream input, ZipOutputStream output) throws IOException {
        output.putNextEntry(entry);
        Util.copyData(input, output);
        output.closeEntry();
    }

    public static class Builder {

        private List<ContainerDocument> documents;
        private List<Pair<String, ContainerAnnotation>> annotations;
        private Pair<String, DataFilesManifest> dataManifest;
        private Pair<String, AnnotationsManifest> annotationsManifest;
        private Pair<String, SignatureManifest> manifest;
        private List<Pair<String, AnnotationInfoManifest>> annotationInfoManifests;

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

        public Builder withAnnotationInfoManifests(List<Pair<String, AnnotationInfoManifest>> annotationManifests) {
            this.annotationInfoManifests = annotationManifests;
            return this;
        }

        public ZipSignatureContent build() {
            return new ZipSignatureContent(documents, annotations, dataManifest, annotationsManifest, manifest, annotationInfoManifests);
        }
    }

}
