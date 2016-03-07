package com.guardtime.container.packaging.zip;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.annotation.FileAnnotation;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.datafile.FileContainerDocument;
import com.guardtime.container.manifest.*;
import com.guardtime.container.packaging.MimeType;
import com.guardtime.container.packaging.SignatureContent;
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
import java.util.ArrayList;
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

    private SignatureContent buildSignature(String manifestName) {
        SignatureManifest manifest = manifestHandler.get(manifestName);
        FileReference dataFileReference = manifest.getDataFilesReference();
        String dataFileReferenceUri = dataFileReference.getUri();
        DataFilesManifest dataManifest = dataManifestHandler.get(dataFileReferenceUri);

        List<? extends FileReference> documentReferences = dataManifest.getDataFileReferences();
        List<ContainerDocument> documents = getDocuments(documentReferences);

        FileReference annotationsManifestReference = manifest.getAnnotationsManifestReference();
        AnnotationsManifest annotationsManifest = annotationsManifestHandler.get(annotationsManifestReference.getUri());
        List<? extends FileReference> annotationManifestReferences = annotationsManifest.getAnnotationManifestReferences();
        List<Pair<String, AnnotationInfoManifest>> annotationReferences = getAnnotationManifests(annotationManifestReferences);

        List<Pair<String, ContainerAnnotation>> annotations = getAnnotations(annotationManifestReferences);

        //TODO check annotation and data file names inside the container
        ZipSignatureContent signatureContent = new ZipSignatureContent.Builder()
                .withDocuments(documents)
                .withManifest(Pair.of(manifestName, manifest))
                .withDataManifest(Pair.of(dataFileReferenceUri, dataManifest))
                .withAnnotationsManifest(Pair.of(annotationsManifestReference.getUri(), annotationsManifest))
                .withAnnotationManifests(annotationReferences)
                .withAnnotations(annotations)
                .build();

        signatureContent.setSignature(signatureHandler.get(manifest.getSignatureReference().getUri()));
        return signatureContent;
    }

    private List<Pair<String, ContainerAnnotation>> getAnnotations(List<? extends FileReference> manifestReferences) {
        List<Pair<String, ContainerAnnotation>> annotations = new ArrayList<>();
        for (FileReference manifestReference : manifestReferences) {
            String reference = manifestReference.getUri();
            AnnotationInfoManifest manifest = annotationManifestHandler.get(reference);
            AnnotationReference annotReference = manifest.getAnnotationReference();
            ContainerAnnotationType type = ContainerAnnotationType.fromContent(manifestReference.getUri());
            File annotationFile = annotationContentHandler.get(annotReference.getUri());
            ContainerAnnotation annotation = new FileAnnotation(annotationFile, annotReference.getUri(), annotReference.getDomain(), type);
            annotations.add(Pair.of(annotationFile.getName(), annotation));
        }
        return annotations;
    }

    private List<Pair<String, AnnotationInfoManifest>> getAnnotationManifests(List<? extends FileReference> manifestReferences) {
        List<Pair<String, AnnotationInfoManifest>> manifests = new LinkedList<>();
        for (FileReference manifestReference : manifestReferences) {
            String reference = manifestReference.getUri();
            AnnotationInfoManifest manifest = annotationManifestHandler.get(reference);
            manifests.add(Pair.of(reference, manifest));
        }
        return manifests;
    }

    private List<ContainerDocument> getDocuments(List<? extends FileReference> references) {
        List<ContainerDocument> documents = new LinkedList<>();
        for (FileReference reference : references) {
            File file = documentHandler.get(reference.getUri());
            documents.add(new FileContainerDocument(file, reference.getMimeType(), reference.getUri()));
        }
        return documents;
    }

    private File createTempFile() throws IOException {
        return Util.createTempFile("ksie_", ".tmp");
    }

}
