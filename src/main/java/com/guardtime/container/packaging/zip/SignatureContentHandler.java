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

    private static final Logger logger = LoggerFactory.getLogger(SignatureContentHandler.class);

    private final DataFileContentHandler documentHandler;
    private final AnnotationContentHandler annotationContentHandler;
    private final ManifestHolder manifestHandler;
    private final DataManifestHandler dataManifestHandler;
    private final AnnotationsManifestHandler annotationsManifestHandler;
    private final AnnotationManifestHandler annotationManifestHandler;
    private final SignatureHandler signatureHandler;

    private Pair<String, SignatureManifest> manifest;
    private Pair<String, DataFilesManifest> dataManifest;
    private Pair<String, AnnotationsManifest> annotationsManifest;
    private List<Pair<String, AnnotationInfoManifest>> annotationManifests;
    private List<Pair<String, ContainerAnnotation>> annotations;
    private List<ContainerDocument> documents;
    private ContainerSignature signature;

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

    public ZipSignatureContent get(String manifestPath) {
        initializeMembers();
        gatherMembersFromHandlers(manifestPath);

        return getSignatureContent();
    }

    private void initializeMembers() {
        manifest = null;
        dataManifest = null;
        annotationsManifest = null;
        signature = null;
        annotationManifests = new LinkedList<>();
        annotations = new LinkedList<>();
        documents = new LinkedList<>();
    }

    private void gatherMembersFromHandlers(String manifestPath) {
        manifest = getManifest(manifestPath);
        dataManifest = getDataManifest();
        annotationsManifest = getAnnotationsManifest();

        populateAnnotationsWithManifests();
        populateDocuments();
        fetchSignature();
    }

    private Pair<String, SignatureManifest> getManifest(String manifestPath) {
        try {
            return Pair.of(manifestPath, manifestHandler.get(manifestPath));
        } catch (FileParsingException e) {
            logger.info("Manifest '{}' failed to parse", manifestPath);
            return null;
        }
    }

    private ZipSignatureContent getSignatureContent() {
        ZipSignatureContent signatureContent = new ZipSignatureContent.Builder()
                .withDocuments(documents)
                .withAnnotations(annotations)
                .withManifest(manifest)
                .withDataManifest(dataManifest)
                .withAnnotationsManifest(annotationsManifest)
                .withAnnotationManifests(annotationManifests)
                .build();

        signatureContent.setSignature(signature);
        return signatureContent;
    }

    private Pair<String, AnnotationsManifest> getAnnotationsManifest() {
        FileReference annotationsManifestReference = manifest.getRight().getAnnotationsManifestReference();
        try {
            return Pair.of(
                    annotationsManifestReference.getUri(),
                    annotationsManifestHandler.get(annotationsManifestReference.getUri())
            );
        } catch (FileParsingException e) {
            logger.info("Manifest '{}' failed to parse", annotationsManifestReference.getUri());
            return null;
        }
    }

    private Pair<String, DataFilesManifest> getDataManifest() {
        FileReference dataManifestReference = manifest.getRight().getDataFilesReference();
        try {
            return Pair.of(dataManifestReference.getUri(), dataManifestHandler.get(dataManifestReference.getUri()));
        } catch (FileParsingException e) {
            logger.info("Manifest '{}' failed to parse", dataManifestReference.getUri());
            return null;
        }
    }

    private void populateDocuments() {
        if (dataManifest == null) return;
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
        if (annotationsManifest == null) return;
        for (FileReference manifestReference : annotationsManifest.getRight().getAnnotationManifestReferences()) {
            Pair<String, AnnotationInfoManifest> annotationInfoManifest = getAnnotationInfoManifest(manifestReference);
            if (annotationInfoManifest != null) {
                annotationManifests.add(annotationInfoManifest);
                Pair<String, ContainerAnnotation> annotation = getContainerAnnotation(manifestReference, annotationInfoManifest.getRight());
                if (annotation != null) annotations.add(annotation);
            }
        }
    }

    private Pair<String, AnnotationInfoManifest> getAnnotationInfoManifest(FileReference manifestReference) {
        Pair<String, AnnotationInfoManifest> returnable = null;
        String manifestReferenceUri = manifestReference.getUri();
        AnnotationInfoManifest annotationInfoManifest = annotationManifestHandler.get(manifestReferenceUri);
        if (annotationInfoManifest != null) {
            returnable = Pair.of(manifestReferenceUri, annotationInfoManifest);
        }
        return returnable;
    }

    private Pair<String, ContainerAnnotation> getContainerAnnotation(FileReference manifestReference, AnnotationInfoManifest annotationInfoManifest) {
        Pair<String, ContainerAnnotation> returnable = null;
        AnnotationReference annotationReference = annotationInfoManifest.getAnnotationReference();
        ContainerAnnotationType type = ContainerAnnotationType.fromContent(manifestReference.getMimeType());
        File annotationFile = annotationContentHandler.get(annotationReference.getUri());
        if (annotationFile != null) {
            ContainerAnnotation annotation = new FileAnnotation(annotationFile, annotationReference.getDomain(), type);
            returnable = Pair.of(annotationReference.getUri(), annotation);
        }
        return returnable;
    }

    public void fetchSignature() {
        if (manifest == null) return;
        String signatureUri = manifest.getRight().getSignatureReference().getUri();
        try {
            signature = signatureHandler.get(signatureUri);
        } catch (FileParsingException e) {
            logger.info("No valid signature in container at '{}'", signatureUri);
            signature = null;
        }
    }
}