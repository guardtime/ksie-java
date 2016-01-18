package com.guardtime.container.packaging.zip;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDataFile;
import com.guardtime.container.manifest.AnnotationInfoManifest;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.SignatureManifest;
import com.guardtime.container.packaging.BlockchainContainer;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class ZipBlockchainContainer implements BlockchainContainer {

    private DataFilesManifest dataFilesManifest;
    private AnnotationsManifest annotationsManifest;
    private SignatureManifest signatureManifest;
    private List<AnnotationInfoManifest> annotationInfoManifests;

    private List<ContainerDataFile> dataFiles = new LinkedList<>();
    private List<ContainerAnnotation> annotations = new LinkedList<>();
    private List<ContainerSignature> signatures = new LinkedList<>();

    private MimeTypeEntry mimeType;

    public ZipBlockchainContainer(List<ContainerDataFile> dataFiles, List<ContainerAnnotation> annotations) {
        this.dataFiles = dataFiles;
        this.annotations = annotations;
        this.mimeType = new MimeTypeEntry();
    }

    public void setAnnotationsManifest(AnnotationsManifest annotationsManifest) {
        this.annotationsManifest = annotationsManifest;
    }

    public void setDataFilesManifest(DataFilesManifest dataFilesManifest) {
        this.dataFilesManifest = dataFilesManifest;
    }

    public void setSignatureManifest(SignatureManifest signatureManifest) {
        this.signatureManifest = signatureManifest;
    }

    public void setAnnotationInfoManifests(List<AnnotationInfoManifest> annotationInfoManifests) {
        this.annotationInfoManifests = annotationInfoManifests;
    }

    public DataHash getSignatureInputHash() {
        return signatureManifest.getDataHash();
    }

    public void addSignature(ContainerSignature signature) {
        signatures.add(signature);
    }

    @Override
    public void writeTo(OutputStream output) throws IOException {
        ZipOutputStream zipOutputStream = new ZipOutputStream(output);
        writeEntry(new ZipEntry(mimeType.getUri()), mimeType.getInputStream(), zipOutputStream);

        writeEntry(new ZipEntry(dataFilesManifest.getUri()), dataFilesManifest.getInputStream(), zipOutputStream);
        writeEntry(new ZipEntry(annotationsManifest.getUri()), annotationsManifest.getInputStream(), zipOutputStream);
        writeEntry(new ZipEntry(signatureManifest.getUri()), signatureManifest.getInputStream(), zipOutputStream);
    }

    private void writeEntry(ZipEntry entry, InputStream input, ZipOutputStream output) throws IOException {
        output.putNextEntry(entry);
        Util.copyData(input, output);
        output.closeEntry();
    }

}
