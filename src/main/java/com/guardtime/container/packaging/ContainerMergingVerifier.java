package com.guardtime.container.packaging;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.UnknownDocument;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.manifest.ManifestFactoryType;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.exception.AnnotationsManifestMergingException;
import com.guardtime.container.packaging.exception.ContainerAnnotationMergingException;
import com.guardtime.container.packaging.exception.ContainerMergingException;
import com.guardtime.container.packaging.exception.DocumentsManifestMergingException;
import com.guardtime.container.packaging.exception.ManifestMergingException;
import com.guardtime.container.packaging.exception.MimeTypeMergingException;
import com.guardtime.container.packaging.exception.SignatureMergingException;
import com.guardtime.container.packaging.exception.SingleAnnotationManifestMergingException;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.util.DataHashException;
import com.guardtime.container.util.Pair;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.DataHasher;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ContainerMergingVerifier {

    public static void verifySameMimeType(Container newContainer, Container existingContainer) throws ContainerMergingException {
        try {
            if (!contentsMatch(existingContainer.getMimeType().getInputStream(), newContainer.getMimeType().getInputStream())) {
                throw new MimeTypeMergingException("Incompatible Container provided for merging!");
            }
        } catch (IOException e) {
            throw new MimeTypeMergingException("Failed to verify container acceptability!", e);
        }
    }

    public static void verifyNewSignatureContentIsAcceptable(SignatureContent newContent, List<SignatureContent> existingContents)
            throws ContainerMergingException {
        if (existingContents.isEmpty()) {
            return;
        }
        SignatureContent existingSignatureContent = existingContents.get(0);
        verifySameManifestType(newContent, existingSignatureContent);
        verifySameSignatureType(newContent, existingSignatureContent);
    }

    public static void verifyUniqueness(SignatureContent newContent, List<SignatureContent> existingContents)
            throws ContainerMergingException {
        try {
            for (SignatureContent existingContent : existingContents) {
                verifyNonClashingManifests(newContent, existingContent);
                verifyNonClashingDocumentsManifests(newContent, existingContent);
                verifyNonClashingAnnotationsManifests(newContent, existingContent);
                verifyNonClashingSignatures(newContent, existingContent);
                verifyNonClashingSingleAnnotationManifests(newContent, existingContent);
                verifyNonClashingAnnotations(newContent, existingContent);
                verifyNonClashingContainerDocuments(newContent, existingContent);
            }
        } catch (IOException e) {
            throw new ContainerMergingException("Failed to verify uniqueness!", e);
        }
    }

    public static void verifyUniqueUnknownFiles(Container newContainer, Container existingContainer)
            throws ContainerMergingException, IOException {
        for (UnknownDocument unknownDocument : newContainer.getUnknownFiles()) {
            checkUniqueness(unknownDocument.getFileName(), unknownDocument, existingContainer);
        }

        for (UnknownDocument unknownDocument : existingContainer.getUnknownFiles()) {
            checkUniqueness(unknownDocument.getFileName(), unknownDocument, newContainer);
        }
    }

    private static void checkUniqueness(String fileName, UnknownDocument unknownDocument, Container existingContainer)
            throws ContainerMergingException, IOException {
        for (SignatureContent content : existingContainer.getSignatureContents()) {
            checkDocuments(fileName, unknownDocument, content.getDocuments().values());
            checkAnnotations(fileName, unknownDocument, content);
            checkManifests(fileName, unknownDocument, content);
            checkSignature(fileName, unknownDocument, content);
        }
        checkDocuments(fileName, unknownDocument, existingContainer.getUnknownFiles());
    }

    private static void checkSignature(String fileName, UnknownDocument unknownDocument, SignatureContent content)
            throws ContainerMergingException, IOException {
        String existingSignatureUri = content.getManifest().getRight().getSignatureReference().getUri();
        if (existingSignatureUri.equals(fileName)) {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(toByteArray(content.getContainerSignature()))) {
                if (!contentsMatch(bis, unknownDocument.getInputStream())) {
                    throw new SignatureMergingException("New SignatureContent has clashing signature! Path: " + fileName);
                }
            }
        }
    }

    private static void checkManifests(String fileName, UnknownDocument unknownDocument, SignatureContent content)
            throws ContainerMergingException, IOException {
        if (content.getManifest().getLeft().equals(fileName)) {
            if (!contentsMatch(content.getManifest().getRight().getInputStream(), unknownDocument.getInputStream())
                    ) {
                throw new ManifestMergingException("New SignatureContent has clashing Manifest! Path: " + fileName);
            }
        } else if (content.getDocumentsManifest().getLeft().equals(fileName)) {
            if (!contentsMatch(content.getDocumentsManifest().getRight().getInputStream(), unknownDocument.getInputStream())) {
                throw new DocumentsManifestMergingException(
                        "New SignatureContent has clashing DocumentsManifest! Path: " + fileName
                );
            }
        } else if (content.getAnnotationsManifest().getLeft().equals(fileName)) {
            if (!contentsMatch(content.getAnnotationsManifest().getRight().getInputStream(), unknownDocument.getInputStream())) {
                throw new AnnotationsManifestMergingException(
                        "New SignatureContent has clashing AnnotationsManifest! Path: " + fileName
                );
            }
        } else {
            checkSingleAnnotationManifests(fileName, unknownDocument, content);
        }
    }

    private static void checkSingleAnnotationManifests(String fileName, UnknownDocument unknownDocument,
                                                       SignatureContent existingContent)
            throws ContainerMergingException, IOException {
        if (existingContent.getSingleAnnotationManifests().containsKey(fileName)) {
            SingleAnnotationManifest existingManifest =
                    existingContent.getSingleAnnotationManifests().get(fileName);
            if (!contentsMatch(existingManifest.getInputStream(), unknownDocument.getInputStream())) {
                throw new SingleAnnotationManifestMergingException(
                        "New SignatureContent has clashing SingleAnnotationManifest! Path: " + fileName
                );
            }
        }
    }

    private static void checkAnnotations(String fileName, UnknownDocument unknownDocument, SignatureContent content)
            throws ContainerMergingException, IOException {
        if (content.getAnnotations().containsKey(fileName)) {
            ContainerAnnotation currentAnnotation = content.getAnnotations().get(fileName);
            if (!contentsMatch(currentAnnotation.getInputStream(), unknownDocument.getInputStream())) {
                throw new ContainerAnnotationMergingException(
                        "New SignatureContent has clashing Annotation data! Path: " + fileName
                );
            }
        }
    }

    private static void checkDocuments(String fileName, ContainerDocument newDocument,
                                       Collection<? extends ContainerDocument> documents)
            throws ContainerMergingException, IOException {
        for (ContainerDocument doc : documents) {
            if (doc.getFileName().equals(fileName)) {
                for (HashAlgorithm algorithm : HashAlgorithm.getImplementedHashAlgorithms()) {
                    try {
                        if (!newDocument.getDataHash(algorithm).equals(doc.getDataHash(algorithm))) {
                            throw new ContainerMergingException(
                                    "New SignatureContent has clashing name for ContainerDocument! Path: " + fileName
                            );
                        }
                    } catch (DataHashException e) {
                        // ignore since it is an EmptyContainerDocument that can't generate new hash
                    }
                }
            }
        }
    }

    private static void verifySameManifestType(SignatureContent content, SignatureContent existingSignatureContent)
            throws ContainerMergingException {
        ManifestFactoryType manifestType = existingSignatureContent.getManifest().getRight().getManifestFactoryType();
        ManifestFactoryType newManifestFactoryType = content.getManifest().getRight().getManifestFactoryType();
        if (!manifestType.equals(newManifestFactoryType)) {
            throw new ManifestMergingException("New SignatureContent has different manifest type!");
        }
    }

    private static void verifySameSignatureType(SignatureContent content, SignatureContent existingSignatureContent)
            throws ContainerMergingException {
        String signatureType = existingSignatureContent.getManifest().getRight().getSignatureReference().getType();
        String newSignatureType = content.getManifest().getRight().getSignatureReference().getType();
        if (!signatureType.equals(newSignatureType)) {
            throw new SignatureMergingException("New SignatureContent has different signature type!");
        }
    }

    private static void verifyNonClashingManifests(SignatureContent content, SignatureContent existingContent)
            throws ContainerMergingException, IOException {
        Pair<String, Manifest> newManifest = content.getManifest();
        Pair<String, Manifest> currentManifest = existingContent.getManifest();
        if (currentManifest.getLeft().equals(newManifest.getLeft()) &&
                !contentsMatch(currentManifest.getRight().getInputStream(), newManifest.getRight().getInputStream())
                ) {
            throw new ManifestMergingException("New SignatureContent has clashing Manifest! Path: " + newManifest.getLeft());
        }
    }

    private static void verifyNonClashingDocumentsManifests(SignatureContent content, SignatureContent existingContent)
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

    private static void verifyNonClashingAnnotationsManifests(SignatureContent content, SignatureContent existingContent)
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

    private static void verifyNonClashingSignatures(SignatureContent content, SignatureContent existingContent)
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

    private static void verifyNonClashingSingleAnnotationManifests(SignatureContent content, SignatureContent existingContent)
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

    private static void verifyNonClashingAnnotations(SignatureContent content, SignatureContent existingContent)
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

    private static void verifyNonClashingContainerDocuments(SignatureContent content, SignatureContent existingContent)
            throws ContainerMergingException, IOException {
        for (Map.Entry<String, ContainerDocument> newDocument : content.getDocuments().entrySet()) {
            checkDocuments(newDocument.getKey(), newDocument.getValue(), existingContent.getDocuments().values());
        }
    }

    private static boolean contentsMatch(InputStream firstStream, InputStream secondStream) {
        DataHash first = new DataHasher().addData(firstStream).getHash();
        DataHash second = new DataHasher().addData(secondStream).getHash();
        return first.equals(second);
    }

    private static boolean contentsMatch(ContainerSignature first, ContainerSignature second) throws IOException {
        try (ByteArrayInputStream firstStream = new ByteArrayInputStream(toByteArray(first));
             ByteArrayInputStream secondStream = new ByteArrayInputStream(toByteArray(second))
        ) {
            return contentsMatch(firstStream, secondStream);
        }
    }

    private static byte[] toByteArray(ContainerSignature signature) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            signature.writeTo(bos);
            return bos.toByteArray();
        }
    }

}
