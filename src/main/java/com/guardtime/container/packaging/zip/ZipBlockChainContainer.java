package com.guardtime.container.packaging.zip;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.AnnotationInfoManifest;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.SignatureManifest;
import com.guardtime.container.packaging.BlockChainContainer;
import com.guardtime.container.packaging.MimeType;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.util.Pair;
import com.guardtime.ksi.util.Util;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class ZipBlockChainContainer implements BlockChainContainer {

    private List<SignatureContent> signatureContents = new LinkedList<>();
    private MimeType mimeType;
    private List<Pair<String, File>> unknownFiles = new LinkedList<>();

    public ZipBlockChainContainer(SignatureContent signatureContent, MimeType mimeType) {
        this.signatureContents.add(signatureContent);
        this.mimeType = mimeType;
    }

    public ZipBlockChainContainer(List<SignatureContent> signatureContents, List<Pair<String, File>> unknownFiles, MimeType mimeType) {
        this.signatureContents = signatureContents;
        this.unknownFiles = unknownFiles;
        this.mimeType = mimeType;
    }

    @Override
    public List<SignatureContent> getSignatureContents() {
        return signatureContents;
    }

    @Override
    public void writeTo(OutputStream output) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(output))) {
            writeMimeTypeEntry(zipOutputStream);
            writeSignatures(signatureContents, zipOutputStream);
            writeExcessFiles(zipOutputStream);
        }
    }

    @Override
    public MimeType getMimeType() {
        return mimeType;
    }

    private void writeExcessFiles(ZipOutputStream zipOutputStream) throws IOException {
        for (Pair<String, File> file : unknownFiles) {
            writeEntry(new ZipEntry(file.getLeft()), new FileInputStream(file.getRight()), zipOutputStream);
        }
    }

    private void writeMimeTypeEntry(ZipOutputStream zipOutputStream) throws IOException {
        ZipEntry mimeTypeEntry = new ZipEntry(mimeType.getUri());
        byte[] data = Util.toByteArray(mimeType.getInputStream());
        mimeTypeEntry.setSize(data.length);
        mimeTypeEntry.setCompressedSize(data.length);
        Checksum checksum = new CRC32();
        checksum.update(data, 0, data.length);
        mimeTypeEntry.setCrc(checksum.getValue());
        mimeTypeEntry.setMethod(ZipEntry.STORED);
        writeEntry(mimeTypeEntry, mimeType.getInputStream(), zipOutputStream);
    }

    private void writeSignatures(List<SignatureContent> signatureContents, ZipOutputStream zipOutputStream) throws IOException {
        for (SignatureContent signatureContent : signatureContents) {
            writeSignatureContent(signatureContent, zipOutputStream);
        }
    }

    public void writeSignatureContent(SignatureContent signatureContent, ZipOutputStream output) throws IOException {
        writeDocuments(signatureContent.getDocuments(), output);
        Pair<String, DataFilesManifest> dataManifest = signatureContent.getDataManifest();
        writeEntry(new ZipEntry(dataManifest.getLeft()), dataManifest.getRight().getInputStream(), output);
        writeAnnotations(signatureContent.getAnnotations(), output);
        writeAnnotationInfoManifests(signatureContent.getAnnotationManifests(), output);
        Pair<String, AnnotationsManifest> annotationsManifest = signatureContent.getAnnotationsManifest();
        writeEntry(new ZipEntry(annotationsManifest.getLeft()), annotationsManifest.getRight().getInputStream(), output);
        Pair<String, SignatureManifest> manifest = signatureContent.getSignatureManifest();
        SignatureManifest signatureManifest = manifest.getRight();
        writeEntry(new ZipEntry(manifest.getLeft()), signatureManifest.getInputStream(), output);
        writeSignature(output, signatureManifest, signatureContent.getSignature());
    }

    private void writeAnnotationInfoManifests(List<Pair<String, AnnotationInfoManifest>> annotationManifests, ZipOutputStream output) throws IOException {
        for (Pair<String, AnnotationInfoManifest> manifest : annotationManifests) {
            writeEntry(new ZipEntry(manifest.getLeft()), manifest.getRight().getInputStream(), output);
        }
    }

    private void writeAnnotations(List<Pair<String, ContainerAnnotation>> annotations, ZipOutputStream output) throws IOException {
        for (Pair<String, ContainerAnnotation> annotation : annotations) {
            writeEntry(new ZipEntry(annotation.getLeft()), annotation.getRight().getInputStream(), output);
        }
    }

    private void writeSignature(ZipOutputStream output, SignatureManifest signatureManifest, ContainerSignature signature) throws IOException {
        String signatureUri = signatureManifest.getSignatureReference().getUri();
        ZipEntry signatureEntry = new ZipEntry(signatureUri);
        output.putNextEntry(signatureEntry);
        signature.writeTo(output);
        output.closeEntry();
    }

    private void writeDocuments(List<ContainerDocument> documents, ZipOutputStream zipOutputStream) throws IOException {
        for (ContainerDocument dataFile : documents) {
            writeEntry(new ZipEntry(dataFile.getFileName()), dataFile.getInputStream(), zipOutputStream);
        }
    }

    private void writeEntry(ZipEntry entry, InputStream input, ZipOutputStream output) throws IOException {
        output.putNextEntry(entry);
        Util.copyData(input, output);
        output.closeEntry();
    }

}
