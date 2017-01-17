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
import com.guardtime.container.signature.SignatureFactoryType;
import com.guardtime.container.util.Pair;
import com.guardtime.ksi.util.Util;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
        this(Collections.singletonList(signatureContent), new LinkedList<UnknownDocument>(), mimeType);
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
            writeSignatures(signatureContents, zipOutputStream);
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
    public void add(Container container) throws ContainerMergingException {
        if(!container.getMimeType().equals(mimeType)) {
            throw new ContainerMergingException("Incompatible Container provided for merging!");
        }
        addAll(container.getSignatureContents());
    }

    @Override
    public void addAll(Collection<SignatureContent> contents) throws ContainerMergingException {
        for (SignatureContent content: contents) {
            add(content);
        }
    }

    @Override
    public void add(SignatureContent content) throws ContainerMergingException {
        verifyNewSignatureContentIsAcceptable(content);
        verifyNameDuplicates(content);
        signatureContents.add(content);
    }

    @Override
    protected void finalize() throws Throwable {
        close();
    }

    private void writeExcessFiles(ZipOutputStream zipOutputStream) throws IOException {
        for (UnknownDocument file : unknownFiles) {
            try (InputStream inputStream = file.getInputStream()) {
                writeEntry(new ZipEntry(file.getFileName()), inputStream, zipOutputStream);
            }
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

    private void writeSignatures(List<SignatureContent> signatureContents, ZipOutputStream output) throws IOException {
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

    private void verifyNewSignatureContentIsAcceptable(SignatureContent content) throws ContainerMergingException {
        if(signatureContents.isEmpty()) {
            return;
        }
        SignatureContent existingSignatureContent = signatureContents.get(0);
        ManifestFactoryType manifestType = existingSignatureContent.getManifest().getRight().getManifestFactoryType();
        String signatureType = existingSignatureContent.getManifest().getRight().getSignatureReference().getType();
        ManifestFactoryType newManifestFactoryType = content.getManifest().getRight().getManifestFactoryType();
        String newSignatureType = content.getManifest().getRight().getSignatureReference().getType();
        if(!manifestType.equals(newManifestFactoryType)) {
            throw new ContainerMergingException("New SignatureContent has different manifest type!");
        } else if(!signatureType.equals(newSignatureType)) {
            throw new ContainerMergingException("New SignatureContent has different signature type!");
        }
    }

    private void verifyNameDuplicates(SignatureContent content) throws ContainerMergingException {
        for(SignatureContent existingContent : signatureContents) {
            String newManifestPath = content.getManifest().getLeft();
            String newDocumentsManifestPath = content.getDocumentsManifest().getLeft();
            String newAnnotationsManifestPath = content.getAnnotationsManifest().getLeft();
            String newSignaturePath = content.getManifest().getRight().getSignatureReference().getUri();

            if(existingContent.getManifest().getLeft().equals(newManifestPath)) {
                throw new ContainerMergingException("New SignatureContent has clashing name for Manifest!");
            } else if(existingContent.getDocumentsManifest().getLeft().equals(newDocumentsManifestPath)) {
                throw new ContainerMergingException("New SignatureContent has clashing name for DocumentsManifest!");
            } else if(existingContent.getAnnotationsManifest().getLeft().equals(newAnnotationsManifestPath)) {
                throw new ContainerMergingException("New SignatureContent has clashing name for AnnotationsManifest!");
            } else if(existingContent.getManifest().getRight().getSignatureReference().getUri().equals(newSignaturePath)) {
                throw new ContainerMergingException("New SignatureContent has clashing name for signature!");
            }
            for(String singleAnnotationManifestPath : existingContent.getSingleAnnotationManifests().keySet()) {
                if(content.getSingleAnnotationManifests().containsKey(singleAnnotationManifestPath)) {
                    throw new ContainerMergingException("New SignatureContent has clashing name for SingleAnnotationManifest!");
                }
            }
            for(String annotationPath: existingContent.getAnnotations().keySet()) {
                if(content.getAnnotations().containsKey(annotationPath)) {
                    throw new ContainerMergingException("New SignatureContent has clashing name for Annotation data!");
                }
            }
            // TODO: when we allow same document paths then refactor this to check that hashes match
            for(String documentPath: existingContent.getDocuments().keySet()) {
                if(content.getDocuments().containsKey(documentPath)) {
                    throw new ContainerMergingException("New SignatureContent has clashing name for SingleAnnotationManifest!");
                }
            }
        }
    }

}
