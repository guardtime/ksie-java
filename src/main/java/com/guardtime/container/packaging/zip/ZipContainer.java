package com.guardtime.container.packaging.zip;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.UnknownDocument;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.manifest.ManifestFactoryType;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.ContainerMergingException;
import com.guardtime.container.packaging.MimeType;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.packaging.parsing.ParsingStore;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.util.Pair;
import com.guardtime.ksi.util.Util;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
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

class ZipContainer implements Container {

    private final ParsingStore parsingStore;
    private List<SignatureContent> signatureContents = new LinkedList<>();
    private MimeType mimeType;
    private boolean closed = false;
    private List<UnknownDocument> unknownFiles = new LinkedList<>();

    public ZipContainer(SignatureContent signatureContent, MimeType mimeType) {
        this(Collections.singletonList(signatureContent), Collections.<UnknownDocument>emptyList(), mimeType);
    }

    public ZipContainer(List<SignatureContent> signatureContents, List<UnknownDocument> unknownFiles, MimeType mimeType) {
        this(signatureContents, unknownFiles, mimeType, null);
    }

    public ZipContainer(List<SignatureContent> contents, List<UnknownDocument> unknownFiles, MimeType mimeType, ParsingStore store) {
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
            writeExcessFiles(zipOutputStream);
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
        verifyNewSignatureContentIsAcceptable(content);
        verifyUniquePaths(content);
        signatureContents.add(content);
    }

    @Override
    public void add(Container container) throws ContainerMergingException {
        verifySameMimeType(container);  
        verifyUniqueUnknownFiles(container);
        addAll(container.getSignatureContents());
        unknownFiles.addAll(container.getUnknownFiles());
    }

    @Override
    public void addAll(Collection<SignatureContent> contents) throws ContainerMergingException {
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

    @Override
    public String toString() {
        return this.getClass().toString() + " {" +
                "signatureContents= " + signatureContents +
                ", mimeType= " + mimeType +
                ", closed= " + closed +
                ", unknownFiles= " + unknownFiles +
                '}';
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

    private void writeSignatureContents(List<SignatureContent> signatureContents, ZipOutputStream output) throws IOException {
        for (SignatureContent signatureContent : signatureContents) {
            Pair<String, Manifest> manifest = signatureContent.getManifest();
            Pair<String, DocumentsManifest> documentsManifest = signatureContent.getDocumentsManifest();
            Pair<String, AnnotationsManifest> annotationsManifest = signatureContent.getAnnotationsManifest();
            writeEntry(new ZipEntry(manifest.getLeft()), manifest.getRight().getInputStream(), output);
            writeEntry(new ZipEntry(documentsManifest.getLeft()), documentsManifest.getRight().getInputStream(), output);
            writeEntry(new ZipEntry(annotationsManifest.getLeft()), annotationsManifest.getRight().getInputStream(), output);
            writeSignature(signatureContent.getContainerSignature(), manifest.getRight(), output);
            writeDocuments(signatureContent.getDocuments(), output);
            writeSingleAnnotationManifests(signatureContent.getSingleAnnotationManifests(), output);
            writeAnnotations(signatureContent.getAnnotations(), output);
        }
    }

    private void writeExcessFiles(ZipOutputStream zipOutputStream) throws IOException {
        for (UnknownDocument file : unknownFiles) {
            try (InputStream inputStream = file.getInputStream()) {
                writeEntry(new ZipEntry(file.getFileName()), inputStream, zipOutputStream);
            }
        }
    }

    private void writeSingleAnnotationManifests(Map<String, SingleAnnotationManifest> singleAnnotationManifestMap, ZipOutputStream output) throws IOException {
        for (String uri : singleAnnotationManifestMap.keySet()) {
            SingleAnnotationManifest singleAnnotationManifest = singleAnnotationManifestMap.get(uri);
            writeEntry(new ZipEntry(uri), singleAnnotationManifest.getInputStream(), output);
        }
    }

    private void writeAnnotations(Map<String, ContainerAnnotation> annotations, ZipOutputStream output) throws IOException {
        for (String uri : annotations.keySet()) {
            ContainerAnnotation annotation = annotations.get(uri);
            try (InputStream inputStream = annotation.getInputStream()) {
                writeEntry(new ZipEntry(uri), inputStream, output);
            }
        }
    }

    private void writeSignature(ContainerSignature signature, Manifest manifest, ZipOutputStream output) throws IOException {
        String signatureUri = manifest.getSignatureReference().getUri();
        ZipEntry signatureEntry = new ZipEntry(signatureUri);
        output.putNextEntry(signatureEntry);
        signature.writeTo(output);
        output.closeEntry();
    }

    private void writeDocuments(Map<String, ContainerDocument> documents, ZipOutputStream zipOutputStream) throws IOException {
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

    private void verifySameMimeType(Container container) throws ContainerMergingException {
        try {
            if (!Arrays.equals(
                    Util.toByteArray(mimeType.getInputStream()),
                    Util.toByteArray(container.getMimeType().getInputStream())
            )) {
                throw new ContainerMergingException("Incompatible Container provided for merging!");
            }
        } catch (IOException e) {
            throw new ContainerMergingException("Failed to verify container acceptability!", e);
        }
    }

    private void verifyNewSignatureContentIsAcceptable(SignatureContent content) throws ContainerMergingException {
        if (signatureContents.isEmpty()) {
            return;
        }
        SignatureContent existingSignatureContent = signatureContents.get(0);
        verifySameManifestType(content, existingSignatureContent);
        verifySameSignatureType(content, existingSignatureContent);
    }

    private void verifySameManifestType(SignatureContent content, SignatureContent existingSignatureContent) throws ContainerMergingException {
        ManifestFactoryType manifestType = existingSignatureContent.getManifest().getRight().getManifestFactoryType();
        ManifestFactoryType newManifestFactoryType = content.getManifest().getRight().getManifestFactoryType();
        if (!manifestType.equals(newManifestFactoryType)) {
            throw new ContainerMergingException("New SignatureContent has different manifest type!");
        }
    }

    private void verifySameSignatureType(SignatureContent content, SignatureContent existingSignatureContent) throws ContainerMergingException {
        String signatureType = existingSignatureContent.getManifest().getRight().getSignatureReference().getType();
        String newSignatureType = content.getManifest().getRight().getSignatureReference().getType();
        if (!signatureType.equals(newSignatureType)) {
            throw new ContainerMergingException("New SignatureContent has different signature type!");
        }
    }

    private void verifyUniquePaths(SignatureContent content) throws ContainerMergingException {
        for (SignatureContent existingContent : signatureContents) {
            verifyNonClashingManifestPath(content, existingContent);
            verifyNonClashingDocumentsManifestPath(content, existingContent);
            verifyNonClashingAnnotationsManifestPath(content, existingContent);
            verifyNonClashingSignaturePath(content, existingContent);
            verifyNonClashingSignaleAnnotationManifestPaths(content, existingContent);
            verifyNonClashingAnnotationPaths(content, existingContent);
            verifyNonClashingContainerDocuments(content, existingContent);
        }
    }

    private void verifyNonClashingManifestPath(SignatureContent content, SignatureContent existingContent) throws ContainerMergingException {
        String newManifestPath = content.getManifest().getLeft();
        if (existingContent.getManifest().getLeft().equals(newManifestPath)) {
            throw new ContainerMergingException("New SignatureContent has clashing name for Manifest!");
        }
    }

    private void verifyNonClashingDocumentsManifestPath(SignatureContent content, SignatureContent existingContent) throws ContainerMergingException {
        String newDocumentsManifestPath = content.getDocumentsManifest().getLeft();
        if (existingContent.getDocumentsManifest().getLeft().equals(newDocumentsManifestPath)) {
            throw new ContainerMergingException("New SignatureContent has clashing name for DocumentsManifest!");
        }
    }

    private void verifyNonClashingAnnotationsManifestPath(SignatureContent content, SignatureContent existingContent) throws ContainerMergingException {
        String newAnnotationsManifestPath = content.getAnnotationsManifest().getLeft();
        if (existingContent.getAnnotationsManifest().getLeft().equals(newAnnotationsManifestPath)) {
            throw new ContainerMergingException("New SignatureContent has clashing name for AnnotationsManifest!");
        }
    }

    private void verifyNonClashingSignaturePath(SignatureContent content, SignatureContent existingContent) throws ContainerMergingException {
        String newSignaturePath = content.getManifest().getRight().getSignatureReference().getUri();
        if (existingContent.getManifest().getRight().getSignatureReference().getUri().equals(newSignaturePath)) {
            throw new ContainerMergingException("New SignatureContent has clashing name for signature!");
        }
    }

    private void verifyNonClashingSignaleAnnotationManifestPaths(SignatureContent content, SignatureContent existingContent) throws ContainerMergingException {
        for (String singleAnnotationManifestPath : existingContent.getSingleAnnotationManifests().keySet()) {
            if (content.getSingleAnnotationManifests().containsKey(singleAnnotationManifestPath)) {
                throw new ContainerMergingException("New SignatureContent has clashing name for SingleAnnotationManifest!");
            }
        }
    }

    private void verifyNonClashingAnnotationPaths(SignatureContent content, SignatureContent existingContent) throws ContainerMergingException {
        for (String annotationPath : existingContent.getAnnotations().keySet()) {
            if (content.getAnnotations().containsKey(annotationPath)) {
                throw new ContainerMergingException("New SignatureContent has clashing name for Annotation data!");
            }
        }
    }

    private void verifyNonClashingContainerDocuments(SignatureContent content, SignatureContent existingContent) throws ContainerMergingException {
        // TODO: when we allow same document paths then refactor this to check that hashes match
        for (String documentPath : existingContent.getDocuments().keySet()) {
            if (content.getDocuments().containsKey(documentPath)) {
                throw new ContainerMergingException("New SignatureContent has clashing name for ContainerDocument!");
            }
        }
    }

    private void verifyUniqueUnknownFiles(Container container) throws ContainerMergingException {
        Set<String> unknownDocumentPaths = new HashSet<>();
        for (UnknownDocument unknownDocument : unknownFiles) {
            unknownDocumentPaths.add(unknownDocument.getFileName());
        }
        for (UnknownDocument unknownDocument : container.getUnknownFiles()) {
            if (unknownDocumentPaths.contains(unknownDocument.getFileName())) {
                throw new ContainerMergingException("There are clashing files in the Containers!");
            }
        }
    }

}
