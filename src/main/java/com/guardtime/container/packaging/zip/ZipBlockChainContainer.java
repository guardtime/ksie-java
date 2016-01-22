package com.guardtime.container.packaging.zip;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.AnnotationInfoManifest;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.SignatureManifest;
import com.guardtime.container.packaging.BlockChainContainer;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.util.Util;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class ZipBlockChainContainer implements BlockChainContainer {

    private DataFilesManifest dataFilesManifest;
    private AnnotationsManifest annotationsManifest;
    private SignatureManifest signatureManifest;
    private List<AnnotationInfoManifest> annotationInfoManifests;

    private List<ContainerDocument> dataFiles;
    private List<ContainerAnnotation> annotations;
    private List<ContainerSignature> signatures = new LinkedList<>();

    private MimeTypeEntry mimeType;

    ZipBlockChainContainer(List<ContainerDocument> dataFiles, List<ContainerAnnotation> annotations) {
        this.dataFiles = dataFiles;
        this.annotations = annotations;
        this.mimeType = new MimeTypeEntry();
    }

    void setAnnotationsManifest(AnnotationsManifest annotationsManifest) {
        this.annotationsManifest = annotationsManifest;
    }

    void setDataFilesManifest(DataFilesManifest dataFilesManifest) {
        this.dataFilesManifest = dataFilesManifest;
    }

    void setSignatureManifest(SignatureManifest signatureManifest) {
        this.signatureManifest = signatureManifest;
    }

    void setAnnotationInfoManifests(List<AnnotationInfoManifest> annotationInfoManifests) {
        this.annotationInfoManifests = annotationInfoManifests;
    }

    DataHash getSignatureInputHash() {
        return signatureManifest.getDataHash(HashAlgorithm.SHA2_256);
    }

    public void addSignature(ContainerSignature signature) {
        signatures.add(signature);
    }

    @Override
    public void writeTo(OutputStream output) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(output))) {
            writeEntry(new ZipEntry(mimeType.getUri()), mimeType.getInputStream(), zipOutputStream);
            writeDocuments(zipOutputStream);
            writeAnnotations(zipOutputStream);
            writeEntry(new ZipEntry(dataFilesManifest.getUri()), dataFilesManifest.getInputStream(), zipOutputStream);
            writeAnnotationsInfoManifests(zipOutputStream);
            writeEntry(new ZipEntry(annotationsManifest.getUri()), annotationsManifest.getInputStream(), zipOutputStream);
            writeEntry(new ZipEntry(signatureManifest.getUri()), signatureManifest.getInputStream(), zipOutputStream);
            // TODO write signatures
        }
    }

    private void writeAnnotationsInfoManifests(ZipOutputStream zipOutputStream) throws IOException {
        for (AnnotationInfoManifest annotationInfoManifest : annotationInfoManifests) {
            writeEntry(new ZipEntry(annotationInfoManifest.getUri()), annotationInfoManifest.getInputStream(), zipOutputStream);
        }
    }

    private void writeAnnotations(ZipOutputStream zipOutputStream) throws IOException {
        for (ContainerAnnotation annotation : annotations) {
            writeEntry(new ZipEntry(annotation.getUri()), annotation.getInputStream(), zipOutputStream);
        }
    }

    private void writeDocuments(ZipOutputStream zipOutputStream) throws IOException {
        for (ContainerDocument dataFile : dataFiles) {
            writeEntry(new ZipEntry(dataFile.getFileName()), dataFile.getInputStream(), zipOutputStream);
        }
    }

    private void writeEntry(ZipEntry entry, InputStream input, ZipOutputStream output) throws IOException {
        output.putNextEntry(entry);
        Util.copyData(input, output);
        output.closeEntry();
    }

}
