/*
 * Copyright 2013-2017 Guardtime, Inc.
 *
 * This file is part of the Guardtime client SDK.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES, CONDITIONS, OR OTHER LICENSES OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * "Guardtime" and "KSI" are trademarks or registered trademarks of
 * Guardtime, Inc., and no license to trademarks is granted; Guardtime
 * reserves and retains all trademark rights.
 */

package com.guardtime.envelope.packaging.zip;

import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.document.Document;
import com.guardtime.envelope.document.UnknownDocument;
import com.guardtime.envelope.manifest.AnnotationsManifest;
import com.guardtime.envelope.manifest.DocumentsManifest;
import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.manifest.SingleAnnotationManifest;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopeWriter;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.signature.EnvelopeSignature;
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

public class ZipEnvelopeWriter implements EnvelopeWriter {

    @Override
    public void write(Envelope envelope, OutputStream output) throws IOException {
        if (envelope.isClosed()) {
            throw new IOException("Can't write closed object!");
        }
        Set<String> writtenFiles = new HashSet<>();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(output))) {
            writeMimeTypeEntry(zipOutputStream, writtenFiles);
            writeSignatureContents(envelope.getSignatureContents(), zipOutputStream, writtenFiles);
            writeUnknownFiles(envelope.getUnknownFiles(), zipOutputStream, writtenFiles);
        }
    }

    private void writeMimeTypeEntry(ZipOutputStream zipOutputStream, Set<String> writtenFiles)
            throws IOException {
        ZipEntry mimeTypeEntry = new ZipEntry(MIME_TYPE_ENTRY_NAME);
        byte[] data = ZipEnvelopePackagingFactoryBuilder.MIME_TYPE.getBytes();
        mimeTypeEntry.setSize(data.length);
        mimeTypeEntry.setCompressedSize(data.length);
        Checksum checksum = new CRC32();
        checksum.update(data, 0, data.length);
        mimeTypeEntry.setCrc(checksum.getValue());
        mimeTypeEntry.setMethod(ZipEntry.STORED);

        zipOutputStream.putNextEntry(mimeTypeEntry);
        zipOutputStream.write(data);
        zipOutputStream.closeEntry();
        writtenFiles.add(MIME_TYPE_ENTRY_NAME);
    }

    private void writeSignatureContents(List<SignatureContent> signatureContents, ZipOutputStream output,
                                        Set<String> writtenFiles) throws IOException {
        for (SignatureContent signatureContent : signatureContents) {
            Manifest manifest = signatureContent.getManifest();
            DocumentsManifest documentsManifest = signatureContent.getDocumentsManifest();
            AnnotationsManifest annotationsManifest = signatureContent.getAnnotationsManifest();
            writeEntry(manifest.getPath(), manifest.getInputStream(), output, writtenFiles);
            writeEntry(documentsManifest.getPath(), documentsManifest.getInputStream(), output, writtenFiles);
            writeEntry(annotationsManifest.getPath(), annotationsManifest.getInputStream(), output, writtenFiles);
            writeSignature(signatureContent.getEnvelopeSignature(), manifest, output, writtenFiles);
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

    private void writeAnnotations(Map<String, Annotation> annotations, ZipOutputStream output, Set<String> writtenFiles)
            throws IOException {
        for (String uri : annotations.keySet()) {
            Annotation annotation = annotations.get(uri);
            try (InputStream inputStream = annotation.getInputStream()) {
                writeEntry(uri, inputStream, output, writtenFiles);
            }
        }
    }

    private void writeSignature(EnvelopeSignature signature, Manifest manifest, ZipOutputStream output, Set<String> writtenFiles)
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

    private void writeDocuments(Map<String, Document> documents, ZipOutputStream zipOutputStream,
                                Set<String> writtenFiles) throws IOException {
        for (String uri : documents.keySet()) {
            Document document = documents.get(uri);
            if (invalidDocumentName(document.getFileName())) {
                throw new IOException(document.getFileName() + " is an invalid document file name!");
            }
            if (document.isWritable()) {
                try (InputStream inputStream = document.getInputStream()) {
                    writeEntry(uri, inputStream, zipOutputStream, writtenFiles);
                }
            }
        }
    }

    /**
     * Filename can't be directory for an {@link Document}. KSIE-54
     */
    private boolean invalidDocumentName(String fileName) {
        return fileName.endsWith("/");
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
