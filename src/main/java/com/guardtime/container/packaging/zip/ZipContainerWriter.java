package com.guardtime.container.packaging.zip;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.UnknownDocument;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerWriter;
import com.guardtime.container.packaging.MimeType;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.util.Pair;
import com.guardtime.ksi.util.Util;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class ZipContainerWriter implements ContainerWriter {

    @Override
    public void write(Container container, OutputStream output) throws IOException {
        Set<String> writtenFiles = new HashSet<>();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(output))) {
            writeMimeTypeEntry(container.getMimeType(), zipOutputStream, writtenFiles);
            writeSignatureContents(container.getSignatureContents(), zipOutputStream, writtenFiles);
            writeUnknownFiles(container.getUnknownFiles(), zipOutputStream, writtenFiles);
        }
    }

    private void writeMimeTypeEntry(MimeType mimeType, ZipOutputStream zipOutputStream, Set<String> writtenFiles)
            throws IOException {
        ZipEntry mimeTypeEntry = new ZipEntry(mimeType.getUri());
        byte[] data = Util.toByteArray(mimeType.getInputStream());
        mimeTypeEntry.setSize(data.length);
        mimeTypeEntry.setCompressedSize(data.length);
        Checksum checksum = new CRC32();
        checksum.update(data, 0, data.length);
        mimeTypeEntry.setCrc(checksum.getValue());
        mimeTypeEntry.setMethod(ZipEntry.STORED);

        zipOutputStream.putNextEntry(mimeTypeEntry);
        zipOutputStream.write(data);
        zipOutputStream.closeEntry();
        writtenFiles.add(mimeType.getUri());
    }

    private void writeSignatureContents(List<SignatureContent> signatureContents, ZipOutputStream output,
                                        Set<String> writtenFiles) throws IOException {
        for (SignatureContent signatureContent : signatureContents) {
            Pair<String, Manifest> manifest = signatureContent.getManifest();
            Pair<String, DocumentsManifest> documentsManifest = signatureContent.getDocumentsManifest();
            Pair<String, AnnotationsManifest> annotationsManifest = signatureContent.getAnnotationsManifest();
            writeEntry(manifest.getLeft(), manifest.getRight().getInputStream(), output, writtenFiles);
            writeEntry(documentsManifest.getLeft(), documentsManifest.getRight().getInputStream(), output, writtenFiles);
            writeEntry(annotationsManifest.getLeft(), annotationsManifest.getRight().getInputStream(), output, writtenFiles);
            writeSignature(signatureContent.getContainerSignature(), manifest.getRight(), output, writtenFiles);
            writeDocuments(signatureContent.getDocuments(), output, writtenFiles);
            writeSingleAnnotationManifests(signatureContent.getSingleAnnotationManifests(), output, writtenFiles);
            writeAnnotations(signatureContent.getAnnotations(), output, writtenFiles);
        }
    }

    private void writeUnknownFiles(List<UnknownDocument> unknownFiles, ZipOutputStream zipOutputStream, Set<String> writtenFiles)
            throws IOException {
        for (UnknownDocument file : unknownFiles) {
            try (InputStream inputStream = file.getInputStream()) {
                writeEntry(file.getFileName(), inputStream, zipOutputStream, writtenFiles);
            }
        }
    }

    private void writeSingleAnnotationManifests(Map<String, SingleAnnotationManifest> singleAnnotationManifestMap,
                                                ZipOutputStream output, Set<String> writtenFiles) throws IOException {
        for (String uri : singleAnnotationManifestMap.keySet()) {
            SingleAnnotationManifest singleAnnotationManifest = singleAnnotationManifestMap.get(uri);
            writeEntry(uri, singleAnnotationManifest.getInputStream(), output, writtenFiles);
        }
    }

    private void writeAnnotations(Map<String, ContainerAnnotation> annotations, ZipOutputStream output, Set<String> writtenFiles)
            throws IOException {
        for (String uri : annotations.keySet()) {
            ContainerAnnotation annotation = annotations.get(uri);
            try (InputStream inputStream = annotation.getInputStream()) {
                writeEntry(uri, inputStream, output, writtenFiles);
            }
        }
    }

    private void writeSignature(ContainerSignature signature, Manifest manifest, ZipOutputStream output, Set<String> writtenFiles)
            throws IOException {
        String signatureUri = manifest.getSignatureReference().getUri();
        if (writtenFiles.contains(signatureUri)) {
            // Skip since the file has already been written from another SignatureContent
            return;
        }
        ZipEntry signatureEntry = new ZipEntry(signatureUri);
        output.putNextEntry(signatureEntry);
        signature.writeTo(output);
        output.closeEntry();
        writtenFiles.add(signatureUri);
    }

    private void writeDocuments(Map<String, ContainerDocument> documents, ZipOutputStream zipOutputStream,
                                Set<String> writtenFiles) throws IOException {
        for (String uri : documents.keySet()) {
            ContainerDocument document = documents.get(uri);
            if (document.isWritable()) {
                try (InputStream inputStream = document.getInputStream()) {
                    writeEntry(uri, inputStream, zipOutputStream, writtenFiles);
                }
            }
        }
    }

    private void writeEntry(String path, InputStream input, ZipOutputStream output, Set<String> writtenFiles) throws IOException {
        if (writtenFiles.contains(path)) {
            // Skip since the file has already been written from another SignatureContent
            return;
        }
        output.putNextEntry(new ZipEntry(path));
        Util.copyData(input, output);
        output.closeEntry();
        writtenFiles.add(path);
    }

}
