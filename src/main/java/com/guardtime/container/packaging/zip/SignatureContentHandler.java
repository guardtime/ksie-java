package com.guardtime.container.packaging.zip;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.annotation.ParsedContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.EmptyContainerDocument;
import com.guardtime.container.document.ParsedContainerDocument;
import com.guardtime.container.manifest.AnnotationDataReference;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.packaging.parsing.ParsingStore;
import com.guardtime.container.packaging.zip.handler.AnnotationContentHandler;
import com.guardtime.container.packaging.zip.handler.AnnotationsManifestHandler;
import com.guardtime.container.packaging.zip.handler.ContentParsingException;
import com.guardtime.container.packaging.zip.handler.DocumentContentHandler;
import com.guardtime.container.packaging.zip.handler.DocumentsManifestHandler;
import com.guardtime.container.packaging.zip.handler.ManifestHandler;
import com.guardtime.container.packaging.zip.handler.SignatureHandler;
import com.guardtime.container.packaging.zip.handler.SingleAnnotationManifestHandler;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.util.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

class SignatureContentHandler {

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

    public Pair<SignatureContent, List<Throwable>> get(String manifestPath) throws ContentParsingException {
        SignatureContentGroup group = new SignatureContentGroup(manifestPath);
        SignatureContent signatureContent = new SignatureContent.Builder()
                .withManifest(group.manifest)
                .withDocumentsManifest(group.documentsManifest)
                .withAnnotationsManifest(group.annotationsManifest)
                .withSingleAnnotationManifests(group.singleAnnotationManifests)
                .withDocuments(group.documents)
                .withAnnotations(group.annotations)
                .withSignature(group.signature)
                .build();

        return Pair.of(signatureContent, group.exceptions);
    }

    private class SignatureContentGroup {

        List<Throwable> exceptions = new ArrayList<>();
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
                exceptions.add(e);
                return null;
            }
        }

        private Pair<String, DocumentsManifest> getDocumentsManifest() {
            FileReference documentsManifestReference = manifest.getRight().getDocumentsManifestReference();
            try {
                return Pair.of(documentsManifestReference.getUri(), documentsManifestHandler.get(documentsManifestReference.getUri()));
            } catch (ContentParsingException e) {
                exceptions.add(e);
                return null;
            }
        }

        private void populateDocuments() {
            if (documentsManifest == null) return;
            for (FileReference reference : documentsManifest.getRight().getDocumentReferences()) {
                ContainerDocument containerDocument = fetchDocumentFromHandler(reference);
                if (containerDocument != null) documents.add(containerDocument);
            }
        }

        private ContainerDocument fetchDocumentFromHandler(FileReference reference) {
            if (invalidReference(reference)) return null;
            String documentUri = reference.getUri();
            try {
                ParsingStore streamProvider = documentHandler.get(documentUri);
                return new ParsedContainerDocument(streamProvider, documentUri, reference.getMimeType(), documentUri);
            } catch (ContentParsingException e) {
                // either removed or was never present in the first place, verifier will decide
                return new EmptyContainerDocument(documentUri, reference.getMimeType(), reference.getHashList());
            }
        }

        private boolean invalidReference(FileReference reference) {
            return reference.getUri() == null ||
                    reference.getMimeType() == null ||
                    reference.getHashList() == null ||
                    reference.getHashList().isEmpty();
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
                exceptions.add(e);
                return null;
            }
        }

        private Pair<String, ContainerAnnotation> getContainerAnnotation(FileReference manifestReference, SingleAnnotationManifest singleAnnotationManifest) {
            try {
                ContainerAnnotationType type = ContainerAnnotationType.fromContent(manifestReference.getMimeType());
                if (type == null) {
                    String message = String.format(
                            "Failed to parse annotation for '%s'. Reason: Invalid annotation type: '%s'",
                            manifestReference.getUri(),
                            manifestReference.getMimeType()
                    );
                    exceptions.add(new ContentParsingException(message));
                    return null;
                }
                AnnotationDataReference annotationDataReference = singleAnnotationManifest.getAnnotationReference();
                String uri = annotationDataReference.getUri();
                ParsingStore annotationStreamProvider = annotationContentHandler.get(uri);
                ContainerAnnotation annotation = new ParsedContainerAnnotation(annotationStreamProvider, uri, annotationDataReference.getDomain(), type);
                return Pair.of(uri, annotation);
            } catch (ContentParsingException e) {
                exceptions.add(e);
                return null;
            }
        }

        private void fetchSignature() {
            String signatureUri = manifest.getRight().getSignatureReference().getUri();
            try {
                signature = signatureHandler.get(signatureUri);
            } catch (ContentParsingException e) {
                exceptions.add(e);
                signature = null;
            }
        }
    }
}