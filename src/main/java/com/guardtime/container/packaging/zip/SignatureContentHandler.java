package com.guardtime.container.packaging.zip;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.annotation.FileContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.EmptyContainerDocument;
import com.guardtime.container.document.FileContainerDocument;
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

    private final DocumentContentHandler documentHandler;
    private final AnnotationContentHandler annotationContentHandler;
    private final ManifestHandler manifestHandler;
    private final DocumentsManifestHandler documentsManifestHandler;
    private final AnnotationsManifestHandler annotationsManifestHandler;
    private final SingleAnnotationManifestHandler singleAnnotationManifestHandler;
    private final SignatureHandler signatureHandler;

    public SignatureContentHandler(DocumentContentHandler documentHandler, AnnotationContentHandler annotationContentHandler,
                                   ManifestHandler manifestHandler, DocumentsManifestHandler documentsManifestHandler,
                                   AnnotationsManifestHandler annotationsManifestHandler, SingleAnnotationManifestHandler singleAnnotationManifestHandler,
                                   SignatureHandler signatureHandler) {
        this.documentHandler = documentHandler;
        this.annotationContentHandler = annotationContentHandler;
        this.manifestHandler = manifestHandler;
        this.documentsManifestHandler = documentsManifestHandler;
        this.annotationsManifestHandler = annotationsManifestHandler;
        this.singleAnnotationManifestHandler = singleAnnotationManifestHandler;
        this.signatureHandler = signatureHandler;
    }

    public ZipSignatureContent get(String manifestPath) throws ContentParsingException {
        SignatureContentGroup group = new SignatureContentGroup(manifestPath);
        ZipSignatureContent signatureContent = new ZipSignatureContent.Builder()
                .withManifest(group.manifest)
                .withDocumentsManifest(group.documentsManifest)
                .withAnnotationsManifest(group.annotationsManifest)
                .withSingleAnnotationManifests(group.singleAnnotationManifests)
                .withDocuments(group.documents)
                .withAnnotations(group.annotations)
                .build();

        signatureContent.setSignature(group.signature);
        return signatureContent;
    }

    private class SignatureContentGroup {

        Pair<String, Manifest> manifest;
        Pair<String, DocumentsManifest> documentsManifest;
        Pair<String, AnnotationsManifest> annotationsManifest;
        List<Pair<String, SingleAnnotationManifest>> singleAnnotationManifests = new LinkedList<>();
        List<Pair<String, ContainerAnnotation>> annotations = new LinkedList<>();
        List<ContainerDocument> documents = new LinkedList<>();
        ContainerSignature signature;


        public SignatureContentGroup(String manifestPath) throws ContentParsingException {
            this.manifest = getManifest(manifestPath);
            this.documentsManifest = getDocumentsManifest();
            this.annotationsManifest = getAnnotationsManifest();

            populateAnnotationsWithManifests();
            populateDocuments();
            fetchSignature();
        }

        private Pair<String, Manifest> getManifest(String manifestPath) throws ContentParsingException {
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

        private Pair<String, DocumentsManifest> getDocumentsManifest() {
            FileReference documentsManifestReference = manifest.getRight().getDocumentsManifestReference();
            try {
                return Pair.of(documentsManifestReference.getUri(), documentsManifestHandler.get(documentsManifestReference.getUri()));
            } catch (ContentParsingException e) {
                LOGGER.info("Manifest '{}' failed to parse. Reason: '{}'", documentsManifestReference.getUri(), e.getMessage());
                return null;
            }
        }

        private void populateDocuments() {
            if (documentsManifest == null) return;
            for (FileReference reference : documentsManifest.getRight().getDocumentReferences()) {
                try {
                    ContainerDocument containerDocument = fetchDocumentFromHandler(reference);
                    if (containerDocument != null) documents.add(containerDocument);
                } catch (ContentParsingException e) {
                    throw new RuntimeException("Programming bug! This should never happen. Investigate why DocumentContentHandler#getEntry() threw exception.", e);
                }
            }
        }

        private ContainerDocument fetchDocumentFromHandler(FileReference reference) throws ContentParsingException {
            if (invalidReference(reference)) return null;
            String documentUri = reference.getUri();
            File file = documentHandler.get(documentUri);
            if (file == null) {
                // either removed or was never present in the first place, verifier will decide
                return new EmptyContainerDocument(documentUri, reference.getMimeType(), reference.getHashList());
            } else {
                return new FileContainerDocument(file, reference.getMimeType(), documentUri);
            }
        }

        private boolean invalidReference(FileReference reference) {
            if (reference.getUri() == null ||
                    reference.getMimeType() == null ||
                    reference.getHashList() == null ||
                    reference.getHashList().isEmpty()) {
                return true;
            }
            return false;
        }

        private void populateAnnotationsWithManifests() {
            if (annotationsManifest == null) return;
            for (FileReference manifestReference : annotationsManifest.getRight().getSingleAnnotationManifestReferences()) {
                Pair<String, SingleAnnotationManifest> singleAnnotationManifest = getSingleAnnotationManifest(manifestReference);
                if (singleAnnotationManifest != null) {
                    singleAnnotationManifests.add(singleAnnotationManifest);
                    Pair<String, ContainerAnnotation> annotation = getContainerAnnotation(manifestReference, singleAnnotationManifest.getRight());
                    if (annotation != null) annotations.add(annotation);
                }
            }
        }

        private Pair<String, SingleAnnotationManifest> getSingleAnnotationManifest(FileReference manifestReference) {
            try {
                String manifestReferenceUri = manifestReference.getUri();
                SingleAnnotationManifest singleAnnotationManifest = singleAnnotationManifestHandler.get(manifestReferenceUri);
                return Pair.of(manifestReferenceUri, singleAnnotationManifest);
            } catch (ContentParsingException e) {
                LOGGER.info("Failed to parse annotation manifest for '{}'. Reason: '{}'", manifestReference.getUri(), e.getMessage());
                return null;
            }
        }

        private Pair<String, ContainerAnnotation> getContainerAnnotation(FileReference manifestReference, SingleAnnotationManifest singleAnnotationManifest) {
            try {
                ContainerAnnotationType type = ContainerAnnotationType.fromContent(manifestReference.getMimeType());
                if (type == null) {
                    LOGGER.info("Failed to parse annotation for '{}'. Reason: Invalid annotation type: '{}'", manifestReference.getUri(), manifestReference.getMimeType());
                    return null;
                }
                AnnotationDataReference annotationDataReference = singleAnnotationManifest.getAnnotationReference();
                File annotationFile = annotationContentHandler.get(annotationDataReference.getUri());
                ContainerAnnotation annotation = new FileContainerAnnotation(annotationFile, annotationDataReference.getDomain(), type);
                return Pair.of(annotationDataReference.getUri(), annotation);
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