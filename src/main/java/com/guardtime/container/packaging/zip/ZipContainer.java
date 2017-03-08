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
import com.guardtime.container.packaging.MimeType;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.packaging.exception.AnnotationsManifestMergingException;
import com.guardtime.container.packaging.exception.ContainerAnnotationMergingException;
import com.guardtime.container.packaging.exception.ContainerMergingException;
import com.guardtime.container.packaging.exception.DocumentsManifestMergingException;
import com.guardtime.container.packaging.exception.ManifestMergingException;
import com.guardtime.container.packaging.exception.MimeTypeMergingException;
import com.guardtime.container.packaging.exception.SignatureMergingException;
import com.guardtime.container.packaging.exception.SingleAnnotationManifestMergingException;
import com.guardtime.container.packaging.parsing.ParsingStore;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.util.Pair;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.DataHasher;
import com.guardtime.ksi.util.Util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
        verifyUniqueness(content);
        signatureContents.add(content);
    }

    @Override
    public void add(Container container) throws ContainerMergingException {
        try {
            verifySameMimeType(container);
            verifyUniqueUnknownFiles(container);
            addAll(container.getSignatureContents());
            unknownFiles.addAll(container.getUnknownFiles());
        } catch (IOException e) {
            throw new ContainerMergingException("Failed to verify uniqueness!", e);
        }
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

    private void writeSingleAnnotationManifests(Map<String, SingleAnnotationManifest> singleAnnotationManifestMap,
                                                ZipOutputStream output) throws IOException {
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
                throw new MimeTypeMergingException("Incompatible Container provided for merging!");
            }
        } catch (IOException e) {
            throw new MimeTypeMergingException("Failed to verify container acceptability!", e);
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

    private void verifySameManifestType(SignatureContent content, SignatureContent existingSignatureContent)
            throws ContainerMergingException {
        ManifestFactoryType manifestType = existingSignatureContent.getManifest().getRight().getManifestFactoryType();
        ManifestFactoryType newManifestFactoryType = content.getManifest().getRight().getManifestFactoryType();
        if (!manifestType.equals(newManifestFactoryType)) {
            throw new ManifestMergingException("New SignatureContent has different manifest type!");
        }
    }

    private void verifySameSignatureType(SignatureContent content, SignatureContent existingSignatureContent)
            throws ContainerMergingException {
        String signatureType = existingSignatureContent.getManifest().getRight().getSignatureReference().getType();
        String newSignatureType = content.getManifest().getRight().getSignatureReference().getType();
        if (!signatureType.equals(newSignatureType)) {
            throw new SignatureMergingException("New SignatureContent has different signature type!");
        }
    }

    private void verifyUniqueness(SignatureContent content) throws ContainerMergingException {
        try {
            for (SignatureContent existingContent : signatureContents) {
                verifyNonClashingManifests(content, existingContent);
                verifyNonClashingDocumentsManifests(content, existingContent);
                verifyNonClashingAnnotationsManifests(content, existingContent);
                verifyNonClashingSignatures(content, existingContent);
                verifyNonClashingSingleAnnotationManifests(content, existingContent);
                verifyNonClashingAnnotations(content, existingContent);
                verifyNonClashingContainerDocuments(content, existingContent);
            }
        } catch (IOException e) {
            throw new ContainerMergingException("Failed to verify uniqueness!", e);
        }
    }

    private void verifyNonClashingManifests(SignatureContent content, SignatureContent existingContent)
            throws ContainerMergingException, IOException {
        Pair<String, Manifest> newManifest = content.getManifest();
        Pair<String, Manifest> currentManifest = existingContent.getManifest();
        if (currentManifest.getLeft().equals(newManifest.getLeft()) &&
                !contentsMatch(currentManifest.getRight().getInputStream(), newManifest.getRight().getInputStream())
                ) {
            throw new ManifestMergingException("New SignatureContent has clashing Manifest! Path: " + newManifest.getLeft());
        }
    }

    private void verifyNonClashingDocumentsManifests(SignatureContent content, SignatureContent existingContent)
            throws ContainerMergingException, IOException {
        Pair<String, DocumentsManifest> newDocumentsManifest = content.getDocumentsManifest();
        Pair<String, DocumentsManifest> currentDocumentsManifest = existingContent.getDocumentsManifest();
        if (currentDocumentsManifest.getLeft().equals(newDocumentsManifest.getLeft()) &&
                !contentsMatch(
                        currentDocumentsManifest.getRight().getInputStream(),
                        newDocumentsManifest.getRight().getInputStream()
                )
                ) {
            throw new DocumentsManifestMergingException(
                    "New SignatureContent has clashing DocumentsManifest! Path: " + newDocumentsManifest.getLeft()
            );
        }
    }

    private void verifyNonClashingAnnotationsManifests(SignatureContent content, SignatureContent existingContent)
            throws ContainerMergingException, IOException {
        Pair<String, AnnotationsManifest> newAnnotationsManifest = content.getAnnotationsManifest();
        Pair<String, AnnotationsManifest> currentAnnotationsManifest = existingContent.getAnnotationsManifest();
        if (currentAnnotationsManifest.getLeft().equals(newAnnotationsManifest.getLeft()) &&
                !contentsMatch(
                        currentAnnotationsManifest.getRight().getInputStream(),
                        newAnnotationsManifest.getRight().getInputStream()
                )
                ) {
            throw new AnnotationsManifestMergingException(
                    "New SignatureContent has clashing AnnotationsManifest! Path: " + newAnnotationsManifest.getLeft()
            );
        }
    }

    private void verifyNonClashingSignatures(SignatureContent content, SignatureContent existingContent)
            throws ContainerMergingException, IOException {
        String newSignaturePath = content.getManifest().getRight().getSignatureReference().getUri();
        String currentSignaturePath = existingContent.getManifest().getRight().getSignatureReference().getUri();
        ContainerSignature newSignature = content.getContainerSignature();
        ContainerSignature currentSignature = existingContent.getContainerSignature();
        if (currentSignaturePath.equals(newSignaturePath) &&
                !contentsMatch(currentSignature, newSignature)
                ) {
            throw new SignatureMergingException("New SignatureContent has clashing signature! Path: " + newSignaturePath);
        }
    }

    private void verifyNonClashingSingleAnnotationManifests(SignatureContent content, SignatureContent existingContent)
            throws ContainerMergingException, IOException {
        for (String singleAnnotationManifestPath : existingContent.getSingleAnnotationManifests().keySet()) {
            if (content.getSingleAnnotationManifests().containsKey(singleAnnotationManifestPath)) {
                SingleAnnotationManifest existingManifest =
                        existingContent.getSingleAnnotationManifests().get(singleAnnotationManifestPath);
                SingleAnnotationManifest newManifest = content.getSingleAnnotationManifests().get(singleAnnotationManifestPath);
                if (!contentsMatch(existingManifest.getInputStream(), newManifest.getInputStream())) {
                    throw new SingleAnnotationManifestMergingException(
                            "New SignatureContent has clashing SingleAnnotationManifest! Path: " + singleAnnotationManifestPath
                    );
                }
            }
        }
    }

    private void verifyNonClashingAnnotations(SignatureContent content, SignatureContent existingContent)
            throws ContainerMergingException, IOException {
        for (String annotationPath : existingContent.getAnnotations().keySet()) {
            if (content.getAnnotations().containsKey(annotationPath)) {
                ContainerAnnotation newAnnotation = content.getAnnotations().get(annotationPath);
                ContainerAnnotation currentAnnotation = existingContent.getAnnotations().get(annotationPath);
                if (!contentsMatch(currentAnnotation.getInputStream(), newAnnotation.getInputStream())) {
                    throw new ContainerAnnotationMergingException(
                            "New SignatureContent has clashing Annotation data! Path: " + annotationPath
                    );
                }
            }
        }
    }

    private void verifyNonClashingContainerDocuments(SignatureContent content, SignatureContent existingContent)
            throws ContainerMergingException {
        // TODO: when we allow same document paths then refactor this to check that hashes match
        for (String documentPath : existingContent.getDocuments().keySet()) {
            if (content.getDocuments().containsKey(documentPath)) {
                throw new ContainerMergingException("New SignatureContent has clashing name for ContainerDocument!");
            }
        }
    }

    private void verifyUniqueUnknownFiles(Container container) throws ContainerMergingException, IOException {
        Map<String, UnknownDocument> currentUnknownDocuments = new HashMap<>();
        for (UnknownDocument unknownDocument : unknownFiles) {
            currentUnknownDocuments.put(unknownDocument.getFileName(), unknownDocument);
        }
        Map<String, UnknownDocument> newUnknownDocuments = new HashMap<>();
        for (UnknownDocument unknownDocument : container.getUnknownFiles()) {
            newUnknownDocuments.put(unknownDocument.getFileName(), unknownDocument);
        }

        for (String fileName : newUnknownDocuments.keySet()) {
            if (currentUnknownDocuments.keySet().contains(fileName)) {
                UnknownDocument newDocument = newUnknownDocuments.get(fileName);
                UnknownDocument currentDocument = currentUnknownDocuments.get(fileName);
                if(!contentsMatch(currentDocument.getInputStream(), newDocument.getInputStream()))
                throw new ContainerMergingException("There is clashing unknown file in the Containers! Path: " + fileName);
            }
        }
    }

    private boolean contentsMatch(InputStream firstStream, InputStream secondStream) {
        DataHash first = new DataHasher().addData(firstStream).getHash();
        DataHash second = new DataHasher().addData(secondStream).getHash();
        return first.equals(second);
    }

    private boolean contentsMatch(ContainerSignature first, ContainerSignature second) throws IOException {
        try (
                ByteArrayOutputStream firstBos = new ByteArrayOutputStream();
                ByteArrayOutputStream secondBos = new ByteArrayOutputStream()
        ) {
            first.writeTo(firstBos);
            second.writeTo(secondBos);
            return contentsMatch(
                    new ByteArrayInputStream(firstBos.toByteArray()),
                    new ByteArrayInputStream(secondBos.toByteArray())
            );
        }
    }

}
