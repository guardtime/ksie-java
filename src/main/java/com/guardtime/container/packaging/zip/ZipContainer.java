package com.guardtime.container.packaging.zip;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.UnknownDocument;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.MimeType;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.packaging.exception.ContainerMergingException;
import com.guardtime.container.packaging.parsing.ParsingStore;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.util.Pair;
import com.guardtime.ksi.util.Util;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.guardtime.container.packaging.ContainerMergingVerifier.verifyNewSignatureContentIsAcceptable;
import static com.guardtime.container.packaging.ContainerMergingVerifier.verifySameMimeType;
import static com.guardtime.container.packaging.ContainerMergingVerifier.verifyUniqueUnknownFiles;
import static com.guardtime.container.packaging.ContainerMergingVerifier.verifyUniqueness;

class ZipContainer implements Container {

    private final ParsingStore parsingStore;
    private List<SignatureContent> signatureContents = new LinkedList<>();
    private MimeType mimeType;
    private boolean closed = false;
    private List<UnknownDocument> unknownFiles = new LinkedList<>();
    private Set<String> writtenFiles = new HashSet<>();

    public ZipContainer(SignatureContent signatureContent, MimeType mimeType) {
        this(Collections.singletonList(signatureContent), Collections.<UnknownDocument>emptyList(), mimeType);
    }

    public ZipContainer(List<SignatureContent> signatureContents, List<UnknownDocument> unknownFiles, MimeType mimeType) {
        this(signatureContents, unknownFiles, mimeType, null);
    }

    public ZipContainer(List<SignatureContent> contents, List<UnknownDocument> unknownFiles, MimeType mimeType,
                        ParsingStore store) {
        this.signatureContents.addAll(contents);
        this.unknownFiles.addAll(unknownFiles);
        this.mimeType = mimeType;
        this.parsingStore = store;
    }

    @Override
    public List<SignatureContent> getSignatureContents() {
        return Collections.unmodifiableList(signatureContents);
    }

    @Override
    public void writeTo(OutputStream output) throws IOException {
        if (closed) {
            throw new IOException("Can't write closed object!");
        }
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(output))) {
            writeMimeTypeEntry(zipOutputStream);
            writeSignatureContents(signatureContents, zipOutputStream);
            writeUnknownFiles(zipOutputStream);
        }
    }

    @Override
    public MimeType getMimeType() {
        return mimeType;
    }

    @Override
    public List<UnknownDocument> getUnknownFiles() {
        return Collections.unmodifiableList(unknownFiles);
    }

    @Override
    public void close() throws Exception {
        for (SignatureContent content : getSignatureContents()) {
            for (ContainerAnnotation annotation : content.getAnnotations().values()) {
                annotation.close();
            }

            for (ContainerDocument document : content.getDocuments().values()) {
                document.close();
            }

            for (UnknownDocument f : getUnknownFiles()) {
                f.close();
            }
        }
        if (parsingStore != null) {
            this.parsingStore.close();
        }
        this.closed = true;
    }

    @Override
    public void add(SignatureContent content) throws ContainerMergingException {
        verifyNewSignatureContentIsAcceptable(content, signatureContents);
        verifyUniqueness(content, signatureContents);
        // TODO: In case the provided content has some documents/annotations using a parsing store we need to copy over those since we can't access and control that parsing store
        signatureContents.add(content);
    }

    @Override
    public void add(Container container) throws ContainerMergingException {
        verifySameMimeType(container, this);
        verifyUniqueUnknownFiles(container, this);
        addAll(container.getSignatureContents());
        unknownFiles.addAll(container.getUnknownFiles());
        // TODO: Merge the parsing stores
    }

    @Override
    public void addAll(Collection<? extends SignatureContent> contents) throws ContainerMergingException {
        List<SignatureContent> original = new LinkedList<>(signatureContents);
        try {
            for (SignatureContent content : contents) {
                add(content);
            }
        } catch (Exception e) {
            this.signatureContents = original;
            throw e;
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

        zipOutputStream.putNextEntry(mimeTypeEntry);
        zipOutputStream.write(data);
        zipOutputStream.closeEntry();
    }

    private void writeSignatureContents(List<SignatureContent> signatureContents, ZipOutputStream output) throws IOException {
        for (SignatureContent signatureContent : signatureContents) {
            Pair<String, Manifest> manifest = signatureContent.getManifest();
            Pair<String, DocumentsManifest> documentsManifest = signatureContent.getDocumentsManifest();
            Pair<String, AnnotationsManifest> annotationsManifest = signatureContent.getAnnotationsManifest();
            writeEntry(manifest.getLeft(), manifest.getRight().getInputStream(), output);
            writeEntry(documentsManifest.getLeft(), documentsManifest.getRight().getInputStream(), output);
            writeEntry(annotationsManifest.getLeft(), annotationsManifest.getRight().getInputStream(), output);
            writeSignature(signatureContent.getContainerSignature(), manifest.getRight(), output);
            writeDocuments(signatureContent.getDocuments(), output);
            writeSingleAnnotationManifests(signatureContent.getSingleAnnotationManifests(), output);
            writeAnnotations(signatureContent.getAnnotations(), output);
        }
    }

    private void writeUnknownFiles(ZipOutputStream zipOutputStream) throws IOException {
        for (UnknownDocument file : unknownFiles) {
            try (InputStream inputStream = file.getInputStream()) {
                writeEntry(file.getFileName(), inputStream, zipOutputStream);
            }
        }
    }

    private void writeSingleAnnotationManifests(Map<String, SingleAnnotationManifest> singleAnnotationManifestMap,
                                                ZipOutputStream output) throws IOException {
        for (String uri : singleAnnotationManifestMap.keySet()) {
            SingleAnnotationManifest singleAnnotationManifest = singleAnnotationManifestMap.get(uri);
            writeEntry(uri, singleAnnotationManifest.getInputStream(), output);
        }
    }

    private void writeAnnotations(Map<String, ContainerAnnotation> annotations, ZipOutputStream output) throws IOException {
        for (String uri : annotations.keySet()) {
            ContainerAnnotation annotation = annotations.get(uri);
            try (InputStream inputStream = annotation.getInputStream()) {
                writeEntry(uri, inputStream, output);
            }
        }
    }

    private void writeSignature(ContainerSignature signature, Manifest manifest, ZipOutputStream output) throws IOException {
        String signatureUri = manifest.getSignatureReference().getUri();
        if (writtenFiles.contains(signatureUri)) {
            // Skip since the signature has already been written from another SignatureContent
            return;
        }
        ZipEntry signatureEntry = new ZipEntry(signatureUri);
        output.putNextEntry(signatureEntry);
        signature.writeTo(output);
        output.closeEntry();
        writtenFiles.add(signatureUri);
    }

    private void writeDocuments(Map<String, ContainerDocument> documents, ZipOutputStream zipOutputStream) throws IOException {
        for (String uri : documents.keySet()) {
            ContainerDocument document = documents.get(uri);
            if (document.isWritable()) {
                try (InputStream inputStream = document.getInputStream()) {
                    writeEntry(uri, inputStream, zipOutputStream);
                }
            }
        }
    }

    private void writeEntry(String path, InputStream input, ZipOutputStream output) throws IOException {
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
