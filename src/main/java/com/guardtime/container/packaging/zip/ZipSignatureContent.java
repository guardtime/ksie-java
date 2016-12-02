package com.guardtime.container.packaging.zip;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.util.DataHashException;
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
    private final Pair<String, DocumentsManifest> documentsManifest;
    private final Pair<String, Manifest> manifest;
    private final Pair<String, AnnotationsManifest> annotationsManifest;
    private final Map<String, SingleAnnotationManifest> singleAnnotationManifestMap;
    private ContainerSignature signature;
    private Map<String, ContainerAnnotation> annotations;

    private ZipSignatureContent(List<ContainerDocument> documents,
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

    public Map<String, ContainerDocument> getDocuments() {
        return documents;
    }

    public Map<String, ContainerAnnotation> getAnnotations() {
        return annotations;
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

    public ContainerSignature getContainerSignature() {
        return signature;
    }

    public void setSignature(ContainerSignature signature) {
        this.signature = signature;
    }

    public DataHash getSignatureInputHash() throws DataHashException {
        return manifest.getRight().getDataHash(HashAlgorithm.SHA2_256);
    }

    public void writeTo(ZipOutputStream output) throws IOException {
        writeDocuments(output);
        writeEntry(new ZipEntry(documentsManifest.getLeft()), documentsManifest.getRight().getInputStream(), output);
        writeAnnotations(output);
        writeSingleAnnotationManifests(output);
        writeEntry(new ZipEntry(annotationsManifest.getLeft()), annotationsManifest.getRight().getInputStream(), output);
        writeEntry(new ZipEntry(manifest.getLeft()), manifest.getRight().getInputStream(), output);
        writeSignature(output);
    }

    private void writeSingleAnnotationManifests(ZipOutputStream output) throws IOException {
        for (String uri : singleAnnotationManifestMap.keySet()) {
            SingleAnnotationManifest singleAnnotationManifest = singleAnnotationManifestMap.get(uri);
            writeEntry(new ZipEntry(uri), singleAnnotationManifest.getInputStream(), output);
        }
    }

    private void writeAnnotations(ZipOutputStream output) throws IOException {
        for (String uri : annotations.keySet()) {
            ContainerAnnotation annotation = annotations.get(uri);
            try (InputStream inputStream = annotation.getInputStream()) {
                writeEntry(new ZipEntry(uri), inputStream, output);
            }
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
            ContainerDocument document = documents.get(uri);
            if (document.isWritable()) {
                try (InputStream inputStream = document.getInputStream()) {
                    writeEntry(new ZipEntry(uri), inputStream, zipOutputStream);
                }
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
        private Pair<String, DocumentsManifest> documentsManifest;
        private Pair<String, AnnotationsManifest> annotationsManifest;
        private Pair<String, Manifest> manifest;
        private List<Pair<String, SingleAnnotationManifest>> singleAnnotationManifests;

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

        public ZipSignatureContent build() {
            return new ZipSignatureContent(documents, annotations, documentsManifest, annotationsManifest, manifest, singleAnnotationManifests);
        }
    }

}
