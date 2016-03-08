package com.guardtime.container.packaging.zip;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.annotation.FileAnnotation;
import com.guardtime.container.annotation.MissingAnnotation;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.datafile.EmptyContainerDocument;
import com.guardtime.container.datafile.FileContainerDocument;
import com.guardtime.container.manifest.*;
import com.guardtime.container.packaging.MimeType;
import com.guardtime.container.packaging.zip.handler.*;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.util.Pair;
import com.guardtime.container.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Helper class for reading Zip container.
 */
class ZipContainerReader {

    private static final Logger logger = LoggerFactory.getLogger(ZipContainerReader.class);

    private final DataFileContentHandler documentHandler = new DataFileContentHandler();
    private final AnnotationContentHandler annotationContentHandler = new AnnotationContentHandler();
    private final UnknownFileHandler unknownFileHandler = new UnknownFileHandler();
    private final ManifestHolder manifestHandler;
    private final DataManifestHandler dataManifestHandler;
    private final AnnotationsManifestHandler annotationsManifestHandler;
    private final AnnotationManifestHandler annotationManifestHandler;
    private final SignatureHandler signatureHandler;
    private final MimeTypeHandler mimeTypeHandler;

    private ContentHandler[] handlers;

    ZipContainerReader(ContainerManifestFactory manifestFactory, SignatureFactory signatureFactory) {
        this.manifestHandler = new ManifestHolder(manifestFactory);
        this.dataManifestHandler = new DataManifestHandler(manifestFactory);
        this.annotationsManifestHandler = new AnnotationsManifestHandler(manifestFactory);
        this.annotationManifestHandler = new AnnotationManifestHandler(manifestFactory);
        this.signatureHandler = new SignatureHandler(signatureFactory);
        this.mimeTypeHandler = new MimeTypeHandler();
        this.handlers = new ContentHandler[]{mimeTypeHandler, documentHandler, annotationContentHandler, dataManifestHandler,
                manifestHandler, annotationsManifestHandler, signatureHandler, annotationManifestHandler};
    }

    ZipBlockChainContainer read(InputStream input) throws IOException {
        try (ZipInputStream zipInput = new ZipInputStream(input)) {
            ZipEntry entry;
            while ((entry = zipInput.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    logger.trace("Skipping directory '{}'", entry.getName());
                    continue;
                }
                readEntry(zipInput, entry);
            }
        }
        List<SignatureContent> contents = buildSignatures();
        MimeType mimeType = getMimeType();
        List<Pair<String, File>> unknownFiles = getUnknownFiles();
        return new ZipBlockChainContainer(contents, unknownFiles, mimeType);
    }

    private MimeType getMimeType() {
        String uri = ZipContainerPackagingFactory.MIME_TYPE_ENTRY_NAME;
        byte[] content = mimeTypeHandler.get(uri);
        return new MimeTypeEntry(uri, content);
    }

    private List<Pair<String, File>> getUnknownFiles() {
        List<Pair<String, File>> returnable = new LinkedList<>();
        for (String name : unknownFileHandler.getNames()) {
            returnable.add(Pair.of(name, unknownFileHandler.get(name)));
        }
        return returnable;
    }

    private void readEntry(ZipInputStream zipInput, ZipEntry entry) throws IOException {
        String name = entry.getName();
        File tempFile = createTempFile();
        com.guardtime.ksi.util.Util.copyData(zipInput, new FileOutputStream(tempFile));
        for (ContentHandler handler : handlers) {
            if (handler.isSupported(name)) {
                logger.info("Reading zip entry '{}'. Using handler '{}' ", name, handler.getClass().getName());
                handler.add(name, tempFile);
                return;
            }
        }
        unknownFileHandler.add(name, tempFile);
    }

    private List<SignatureContent> buildSignatures() {
        Set<String> signatureManifests = manifestHandler.getNames();
        List<SignatureContent> signatures = new LinkedList<>();
        for (String manifest : signatureManifests) {
            signatures.add(buildSignature(manifest));
        }
        return signatures;
    }

    private SignatureContent buildSignature(String manifestPath) {
        GroupParser groupParser = new GroupParser(manifestPath);

        //TODO check annotation and data file names inside the container
        SignatureContent signatureContent = new SignatureContent.Builder()
                .withDocuments(groupParser.getDocuments())
                .withManifest(groupParser.getManifest())
                .withDataManifest(groupParser.getDataManifest())
                .withAnnotationsManifest(groupParser.getAnnotationsManifest())
                .withAnnotationManifests(groupParser.getAnnotationManifests())
                .withAnnotations(groupParser.getAnnotations())
                .build();

        String signatureUri = groupParser.getManifest().getRight().getSignatureReference().getUri();
        signatureContent.setSignature(signatureHandler.get(signatureUri));
        return signatureContent;
    }

    private File createTempFile() throws IOException {
        return Util.createTempFile("ksie_", ".tmp");
    }

    private class GroupParser {
        private Pair<String, SignatureManifest> manifest;
        private Pair<String, DataFilesManifest> dataManifest;
        private Pair<String, AnnotationsManifest> annotationsManifest;
        private List<Pair<String, AnnotationInfoManifest>> annotationManifests = new LinkedList<>();
        private List<Pair<String, ContainerAnnotation>> annotations = new LinkedList<>();
        private List<ContainerDocument> documents = new LinkedList<>();

        public GroupParser(String manifestPath) {
            this.manifest = Pair.of(manifestPath, manifestHandler.get(manifestPath));;
            this.dataManifest = getDataManifestPair();
            this.annotationsManifest = getAnnotationsManifestPair();

            populateAnnotationsWithManifests();
            populateDocuments();
        }

        public Pair<String, SignatureManifest> getManifest() {
            return manifest;
        }

        public Pair<String, DataFilesManifest> getDataManifest() {
            return dataManifest;
        }

        public Pair<String, AnnotationsManifest> getAnnotationsManifest() {
            return annotationsManifest;
        }

        public List<Pair<String, AnnotationInfoManifest>> getAnnotationManifests() {
            return annotationManifests;
        }

        public List<Pair<String, ContainerAnnotation>> getAnnotations() {
            return annotations;
        }

        public List<ContainerDocument> getDocuments() {
            return documents;
        }

        private Pair<String, AnnotationsManifest> getAnnotationsManifestPair() {
            FileReference annotationsManifestReference = manifest.getRight().getAnnotationsManifestReference();
            return Pair.of(
                    annotationsManifestReference.getUri(),
                    annotationsManifestHandler.get(annotationsManifestReference.getUri())
            );
        }

        private Pair<String, DataFilesManifest> getDataManifestPair() {
            FileReference datamanifestReference = manifest.getRight().getDataFilesReference();
            return Pair.of(datamanifestReference.getUri(), dataManifestHandler.get(datamanifestReference.getUri()));
        }

        private void populateDocuments() {
            for (FileReference reference : dataManifest.getRight().getDataFileReferences()) {
                String documentUri = reference.getUri();
                File file = documentHandler.get(documentUri);
                if (file == null) {
                    // either removed or was never present in the first place, verifier will decide
                    documents.add(new EmptyContainerDocument(documentUri, reference.getMimeType(), reference.getHash()));
                } else {
                    documents.add(new FileContainerDocument(file, reference.getMimeType(), documentUri));
                }
            }
        }

        private void populateAnnotationsWithManifests() {
            for (FileReference manifestReference : annotationsManifest.getRight().getAnnotationManifestReferences()) {
                Pair<String, AnnotationInfoManifest> annotationInfoManifest = getAnnotationInfoManifest(manifestReference);
                annotationManifests.add(annotationInfoManifest);

                if(annotationInfoManifest.getRight().getAnnotationReference() != null) {
                    Pair<String, ContainerAnnotation> annotation = getContainerAnnotation(manifestReference, annotationInfoManifest.getRight());
                    annotations.add(annotation);
                }
            }
        }

        private Pair<String, AnnotationInfoManifest> getAnnotationInfoManifest(FileReference manifestReference) {
            String manifestReferenceUri = manifestReference.getUri();
            AnnotationInfoManifest annotationInfoManifest = annotationManifestHandler.get(manifestReferenceUri);
            return Pair.of(manifestReferenceUri, annotationInfoManifest);
        }

        private Pair<String, ContainerAnnotation> getContainerAnnotation(FileReference manifestReference, AnnotationInfoManifest annotationInfoManifest) {
            AnnotationReference annotationReference = annotationInfoManifest.getAnnotationReference();
            ContainerAnnotationType type = ContainerAnnotationType.fromContent(manifestReference.getUri());
            File annotationFile = annotationContentHandler.get(annotationReference.getUri());
            ContainerAnnotation annotation;
            if(annotationFile == null) {
                annotation = new MissingAnnotation(type, annotationReference.getDomain(), annotationReference.getHash());
            } else {
                annotation = new FileAnnotation(annotationFile, annotationReference.getUri(), annotationReference.getDomain(), type);
            }
            return Pair.of(annotationReference.getUri(), annotation);
        }
    }
}
