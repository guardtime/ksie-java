package com.guardtime.container.packaging.zip;

import com.guardtime.container.BlockChainContainerException;
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
    private final ManifestHolder manifestHandler;
    private final DataManifestHandler dataManifestHandler;
    private final AnnotationsManifestHandler annotationsManifestHandler;
    private final AnnotationManifestHandler annotationManifestHandler;
    private final SignatureHandler signatureHandler;

    public SignatureContentHandler(DataFileContentHandler documentHandler, AnnotationContentHandler annotationContentHandler,
                                   ManifestHolder manifestHandler, DataManifestHandler dataManifestHandler,
                                   AnnotationsManifestHandler annotationsManifestHandler, AnnotationManifestHandler annotationManifestHandler,
                                   SignatureHandler signatureHandler) {
        this.documentHandler = documentHandler;
        this.annotationContentHandler = annotationContentHandler;
        this.manifestHandler = manifestHandler;
        this.dataManifestHandler = dataManifestHandler;
        this.annotationsManifestHandler = annotationsManifestHandler;
        this.annotationManifestHandler = annotationManifestHandler;
        this.signatureHandler = signatureHandler;
    }

    public ZipSignatureContent get(String manifestPath) throws BlockChainContainerException {
        SignatureContentGroup group = new SignatureContentGroup(manifestPath);
        ZipSignatureContent signatureContent = new ZipSignatureContent.Builder()
                .withManifest(group.manifest)
                .withDataManifest(group.dataManifest)
                .withAnnotationsManifest(group.annotationsManifest)
                .withAnnotationManifests(group.annotationManifests)
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
        List<Pair<String, AnnotationInfoManifest>> annotationManifests = new LinkedList<>();
        List<Pair<String, ContainerAnnotation>> annotations = new LinkedList<>();
        List<ContainerDocument> documents = new LinkedList<>();
        ContainerSignature signature;


        public SignatureContentGroup(String manifestPath) throws BlockChainContainerException {
            this.manifest = getManifest(manifestPath);
            this.dataManifest = getDataManifest();
            this.annotationsManifest = getAnnotationsManifest();

            populateAnnotationsWithManifests();
            populateDocuments();
            fetchSignature();
        }

        private Pair<String, SignatureManifest> getManifest(String manifestPath) throws BlockChainContainerException {
            try {
                return Pair.of(manifestPath, manifestHandler.get(manifestPath));
            } catch (FileParsingException e) {
                LOGGER.info("Manifest '{}' failed to parse", manifestPath);
                throw new BlockChainContainerException(e); // Can't gather SignatureContent without the main manifest
            }
        }

        private Pair<String, AnnotationsManifest> getAnnotationsManifest() {
            FileReference annotationsManifestReference = manifest.getRight().getAnnotationsManifestReference();
            try {
                return Pair.of(
                        annotationsManifestReference.getUri(),
                        annotationsManifestHandler.get(annotationsManifestReference.getUri())
                );
            } catch (FileParsingException e) {
                LOGGER.info("Manifest '{}' failed to parse", annotationsManifestReference.getUri());
                return null;
            }
        }

        private Pair<String, DataFilesManifest> getDataManifest() {
            FileReference dataManifestReference = manifest.getRight().getDataFilesReference();
            try {
                return Pair.of(dataManifestReference.getUri(), dataManifestHandler.get(dataManifestReference.getUri()));
            } catch (FileParsingException e) {
                LOGGER.info("Manifest '{}' failed to parse", dataManifestReference.getUri());
                return null;
            }
        }

        private void populateDocuments() {
            if (dataManifest == null) return;
            for (FileReference reference : dataManifest.getRight().getDataFileReferences()) {
                try {
                    documents.add(fetchDocumentFromHandler(reference));
                } catch (FileParsingException e) {
                    throw new RuntimeException("Programming bug! This should never happen. Investigate why DataFileContentHandler threw exception.", e);
                }
            }
        }

        private ContainerDocument fetchDocumentFromHandler(FileReference reference) throws FileParsingException {
            String documentUri = reference.getUri();
            File file = documentHandler.get(documentUri);
            if(file == null) {
                // either removed or was never present in the first place, verifier will decide
                return new EmptyContainerDocument(documentUri, reference.getMimeType(), reference.getHash());
            } else {
                return new FileContainerDocument(file, reference.getMimeType(), documentUri);
            }
        }

        private void populateAnnotationsWithManifests() {
            if (annotationsManifest == null) return;
            for (FileReference manifestReference : annotationsManifest.getRight().getAnnotationManifestReferences()) {
                try {
                    Pair<String, AnnotationInfoManifest> annotationInfoManifest = getAnnotationInfoManifest(manifestReference);
                    annotationManifests.add(annotationInfoManifest);
                    Pair<String, ContainerAnnotation> annotation = getContainerAnnotation(manifestReference, annotationInfoManifest.getRight());
                    annotations.add(annotation);
                } catch (FileParsingException e) {
                    // drop it since we failed to parse annotation and/or manifest
                }
            }
        }

        private Pair<String, AnnotationInfoManifest> getAnnotationInfoManifest(FileReference manifestReference) throws FileParsingException {
            Pair<String, AnnotationInfoManifest> returnable = null;
            String manifestReferenceUri = manifestReference.getUri();
            AnnotationInfoManifest annotationInfoManifest = annotationManifestHandler.get(manifestReferenceUri);
            if (annotationInfoManifest != null) {
                returnable = Pair.of(manifestReferenceUri, annotationInfoManifest);
            }
            return returnable;
        }

        private Pair<String, ContainerAnnotation> getContainerAnnotation(FileReference manifestReference, AnnotationInfoManifest annotationInfoManifest) throws FileParsingException {
            AnnotationReference annotationReference = annotationInfoManifest.getAnnotationReference();
            ContainerAnnotationType type = ContainerAnnotationType.fromContent(manifestReference.getMimeType());
            File annotationFile = annotationContentHandler.get(annotationReference.getUri());
            ContainerAnnotation annotation = new FileAnnotation(annotationFile, annotationReference.getDomain(), type);
            return Pair.of(annotationReference.getUri(), annotation);
        }

        private void fetchSignature() {
            String signatureUri = manifest.getRight().getSignatureReference().getUri();
            try {
                signature = signatureHandler.get(signatureUri);
            } catch (FileParsingException e) {
                LOGGER.info("No valid signature in container at '{}'", signatureUri);
                signature = null;
            }
        }
    }
}