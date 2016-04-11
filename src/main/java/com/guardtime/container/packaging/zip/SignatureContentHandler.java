package com.guardtime.container.packaging.zip;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.annotation.FileAnnotation;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.datafile.EmptyContainerDocument;
import com.guardtime.container.datafile.FileContainerDocument;
import com.guardtime.container.manifest.*;
import com.guardtime.container.packaging.zip.handler.*;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

class SignatureContentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignatureContentHandler.class);

    private final DataFileContentHandler documentHandler;
    private final AnnotationContentHandler annotationContentHandler;
    private final ManifestHandler manifestHandler;
    private final DataManifestHandler dataManifestHandler;
    private final AnnotationsManifestHandler annotationsManifestHandler;
    private final AnnotationInfoManifestHandler annotationInfoManifestHandler;
    private final SignatureHandler signatureHandler;

    public SignatureContentHandler(DataFileContentHandler documentHandler, AnnotationContentHandler annotationContentHandler,
                                   ManifestHandler manifestHandler, DataManifestHandler dataManifestHandler,
                                   AnnotationsManifestHandler annotationsManifestHandler, AnnotationInfoManifestHandler annotationInfoManifestHandler,
                                   SignatureHandler signatureHandler) {
        this.documentHandler = documentHandler;
        this.annotationContentHandler = annotationContentHandler;
        this.manifestHandler = manifestHandler;
        this.dataManifestHandler = dataManifestHandler;
        this.annotationsManifestHandler = annotationsManifestHandler;
        this.annotationInfoManifestHandler = annotationInfoManifestHandler;
        this.signatureHandler = signatureHandler;
    }

    public ZipSignatureContent get(String manifestPath) throws ContentParsingException {
        SignatureContentGroup group = new SignatureContentGroup(manifestPath);
        ZipSignatureContent signatureContent = new ZipSignatureContent.Builder()
                .withManifest(group.manifest)
                .withDataManifest(group.dataManifest)
                .withAnnotationsManifest(group.annotationsManifest)
                .withAnnotationInfoManifests(group.annotationInfoManifests)
                .withDocuments(group.documents)
                .withAnnotations(group.annotations)
                .build();

        signatureContent.setSignature(group.signature);
        return signatureContent;
    }

    private class SignatureContentGroup {

        Pair<String, SignatureManifest> manifest;
        Pair<String, DataFilesManifest> dataManifest;
        Pair<String, AnnotationsManifest> annotationsManifest;
        List<Pair<String, AnnotationInfoManifest>> annotationInfoManifests = new LinkedList<>();
        List<Pair<String, ContainerAnnotation>> annotations = new LinkedList<>();
        List<ContainerDocument> documents = new LinkedList<>();
        ContainerSignature signature;


        public SignatureContentGroup(String manifestPath) throws ContentParsingException {
            this.manifest = getManifest(manifestPath);
            this.dataManifest = getDataManifest();
            this.annotationsManifest = getAnnotationsManifest();

            populateAnnotationsWithManifests();
            populateDocuments();
            fetchSignature();
        }

        private Pair<String, SignatureManifest> getManifest(String manifestPath) throws ContentParsingException {
            return Pair.of(manifestPath, manifestHandler.get(manifestPath));
        }

        private Pair<String, AnnotationsManifest> getAnnotationsManifest() {
            FileReference annotationsManifestReference = manifest.getRight().getAnnotationsManifestReference();
            try {
                return Pair.of(
                        annotationsManifestReference.getUri(),
                        annotationsManifestHandler.get(annotationsManifestReference.getUri())
                );
            } catch (ContentParsingException e) {
                LOGGER.info("Manifest '{}' failed to parse. Reason: '{}'", annotationsManifestReference.getUri(), e.getMessage());
                return null;
            }
        }

        private Pair<String, DataFilesManifest> getDataManifest() {
            FileReference dataManifestReference = manifest.getRight().getDataFilesManifestReference();
            try {
                return Pair.of(dataManifestReference.getUri(), dataManifestHandler.get(dataManifestReference.getUri()));
            } catch (ContentParsingException e) {
                LOGGER.info("Manifest '{}' failed to parse. Reason: '{}'", dataManifestReference.getUri(), e.getMessage());
                return null;
            }
        }

        private void populateDocuments() {
            if (dataManifest == null) return;
            for (FileReference reference : dataManifest.getRight().getDataFileReferences()) {
                try {
                    documents.add(fetchDocumentFromHandler(reference));
                } catch (ContentParsingException e) {
                    throw new RuntimeException("Programming bug! This should never happen. Investigate why DataFileContentHandler#getEntry() threw exception.", e);
                }
            }
        }

        private ContainerDocument fetchDocumentFromHandler(FileReference reference) throws ContentParsingException {
            String documentUri = reference.getUri();
            File file = documentHandler.get(documentUri);
            if (file == null) {
                // either removed or was never present in the first place, verifier will decide
                return new EmptyContainerDocument(documentUri, reference.getMimeType(), reference.getHash());
            } else {
                return new FileContainerDocument(file, reference.getMimeType(), documentUri);
            }
        }

        private void populateAnnotationsWithManifests() {
            if (annotationsManifest == null) return;
            for (FileReference manifestReference : annotationsManifest.getRight().getAnnotationInfoManifestReferences()) {
                Pair<String, AnnotationInfoManifest> annotationInfoManifest = getAnnotationInfoManifest(manifestReference);
                if (annotationInfoManifest != null) {
                    annotationInfoManifests.add(annotationInfoManifest);
                    Pair<String, ContainerAnnotation> annotation = getContainerAnnotation(manifestReference, annotationInfoManifest.getRight());
                    if (annotation != null) annotations.add(annotation);
                }
            }
        }

        private Pair<String, AnnotationInfoManifest> getAnnotationInfoManifest(FileReference manifestReference) {
            try {
                String manifestReferenceUri = manifestReference.getUri();
                AnnotationInfoManifest annotationInfoManifest = annotationInfoManifestHandler.get(manifestReferenceUri);
                return Pair.of(manifestReferenceUri, annotationInfoManifest);
            } catch (ContentParsingException e) {
                LOGGER.info("Failed to parse annotation manifest for '{}'. Reason: '{}'", manifestReference.getUri(), e.getMessage());
                return null;
            }
        }

        private Pair<String, ContainerAnnotation> getContainerAnnotation(FileReference manifestReference, AnnotationInfoManifest annotationInfoManifest) {
            try {
                AnnotationReference annotationReference = annotationInfoManifest.getAnnotationReference();
                ContainerAnnotationType type = ContainerAnnotationType.fromContent(manifestReference.getMimeType());
                File annotationFile = annotationContentHandler.get(annotationReference.getUri());
                ContainerAnnotation annotation = new FileAnnotation(annotationFile, annotationReference.getDomain(), type);
                return Pair.of(annotationReference.getUri(), annotation);
            } catch (ContentParsingException e) {
                LOGGER.info("Failed to parse annotation for '{}'. Reason: '{}'", manifestReference.getUri(), e.getMessage());
                return null;
            }
        }

        private void fetchSignature() {
            String signatureUri = manifest.getRight().getSignatureReference().getUri();
            try {
                signature = signatureHandler.get(signatureUri);
            } catch (ContentParsingException e) {
                LOGGER.info("No valid signature in container at '{}'", signatureUri);
                signature = null;
            }
        }
    }
}