/*
 * Copyright 2013-2017 Guardtime, Inc.
 *
 * This file is part of the Guardtime client SDK.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES, CONDITIONS, OR OTHER LICENSES OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * "Guardtime" and "KSI" are trademarks or registered trademarks of
 * Guardtime, Inc., and no license to trademarks is granted; Guardtime
 * reserves and retains all trademark rights.
 */

package com.guardtime.envelope.packaging;

import com.guardtime.envelope.annotation.EnvelopeAnnotation;
import com.guardtime.envelope.document.EnvelopeDocument;
import com.guardtime.envelope.document.UnknownDocument;
import com.guardtime.envelope.manifest.AnnotationsManifest;
import com.guardtime.envelope.manifest.DocumentsManifest;
import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.manifest.ManifestFactoryType;
import com.guardtime.envelope.manifest.SingleAnnotationManifest;
import com.guardtime.envelope.packaging.exception.AnnotationsManifestMergingException;
import com.guardtime.envelope.packaging.exception.EnvelopeAnnotationMergingException;
import com.guardtime.envelope.packaging.exception.EnvelopeMergingException;
import com.guardtime.envelope.packaging.exception.DocumentMergingException;
import com.guardtime.envelope.packaging.exception.DocumentsManifestMergingException;
import com.guardtime.envelope.packaging.exception.ManifestMergingException;
import com.guardtime.envelope.packaging.exception.MimeTypeMergingException;
import com.guardtime.envelope.packaging.exception.SignatureMergingException;
import com.guardtime.envelope.packaging.exception.SingleAnnotationManifestMergingException;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.util.DataHashException;
import com.guardtime.envelope.util.Pair;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.DataHasher;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class EnvelopeMergingVerifier {

    public static void verifySameMimeType(Envelope newEnvelope, Envelope existingEnvelope) throws EnvelopeMergingException {
        try {
            if (!contentsMatch(existingEnvelope.getMimeType().getInputStream(), newEnvelope.getMimeType().getInputStream())) {
                throw new MimeTypeMergingException("Incompatible Envelope provided for merging!");
            }
        } catch (IOException e) {
            throw new MimeTypeMergingException("Failed to verify envelope acceptability!", e);
        }
    }

    public static void verifyNewSignatureContentIsAcceptable(SignatureContent newContent, List<SignatureContent> existingContents)
            throws EnvelopeMergingException {
        if (existingContents.isEmpty()) {
            return;
        }
        SignatureContent existingSignatureContent = existingContents.get(0);
        verifySameManifestType(newContent, existingSignatureContent);
        verifySameSignatureType(newContent, existingSignatureContent);
    }

    public static void verifyUniqueness(SignatureContent newContent, List<SignatureContent> existingContents)
            throws EnvelopeMergingException {
        try {
            for (SignatureContent existingContent : existingContents) {
                verifyNonClashingManifests(newContent, existingContent);
                verifyNonClashingDocumentsManifests(newContent, existingContent);
                verifyNonClashingAnnotationsManifests(newContent, existingContent);
                verifyNonClashingSignatures(newContent, existingContent);
                verifyNonClashingSingleAnnotationManifests(newContent, existingContent);
                verifyNonClashingAnnotations(newContent, existingContent);
                verifyNonClashingEnvelopeDocuments(newContent, existingContent);
            }
        } catch (IOException e) {
            throw new EnvelopeMergingException("Failed to verify uniqueness!", e);
        }
    }

    public static void verifyUniqueUnknownFiles(Envelope newEnvelope, Envelope existingEnvelope)
            throws EnvelopeMergingException {
        try {
            for (UnknownDocument unknownDocument : newEnvelope.getUnknownFiles()) {
                checkUniqueness(unknownDocument.getFileName(), unknownDocument, existingEnvelope);
            }

            for (UnknownDocument unknownDocument : existingEnvelope.getUnknownFiles()) {
                checkUniqueness(unknownDocument.getFileName(), unknownDocument, newEnvelope);
            }
        } catch (IOException e) {
            throw new EnvelopeMergingException("Failed to verify uniqueness!", e);
        }
    }

    private static void checkUniqueness(String fileName, UnknownDocument unknownDocument, Envelope existingEnvelope)
            throws EnvelopeMergingException, IOException {
        for (SignatureContent content : existingEnvelope.getSignatureContents()) {
            checkDocuments(fileName, unknownDocument, content.getDocuments().values());
            checkAnnotations(fileName, unknownDocument, content);
            checkManifests(fileName, unknownDocument, content);
            checkSignature(fileName, unknownDocument, content);
        }
        checkDocuments(fileName, unknownDocument, existingEnvelope.getUnknownFiles());
    }

    private static void checkSignature(String fileName, UnknownDocument unknownDocument, SignatureContent content)
            throws EnvelopeMergingException, IOException {
        String existingSignatureUri = content.getManifest().getRight().getSignatureReference().getUri();
        if (existingSignatureUri.equals(fileName)) {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(toByteArray(content.getEnvelopeSignature()))) {
                if (!contentsMatch(bis, unknownDocument.getInputStream())) {
                    throw new SignatureMergingException("New SignatureContent has clashing signature! Path: " + fileName);
                }
            }
        }
    }

    private static void checkManifests(String fileName, UnknownDocument unknownDocument, SignatureContent content)
            throws EnvelopeMergingException, IOException {
        if (content.getManifest().getLeft().equals(fileName)) {
            if (!contentsMatch(content.getManifest().getRight().getInputStream(), unknownDocument.getInputStream())
                    ) {
                throw new ManifestMergingException("New SignatureContent has clashing Manifest! Path: " + fileName);
            }
        } else if (content.getDocumentsManifest().getLeft().equals(fileName)) {
            if (!contentsMatch(content.getDocumentsManifest().getRight().getInputStream(), unknownDocument.getInputStream())) {
                throw new DocumentsManifestMergingException(fileName);
            }
        } else if (content.getAnnotationsManifest().getLeft().equals(fileName)) {
            if (!contentsMatch(content.getAnnotationsManifest().getRight().getInputStream(), unknownDocument.getInputStream())) {
                throw new AnnotationsManifestMergingException(fileName);
            }
        } else {
            checkSingleAnnotationManifests(fileName, unknownDocument, content);
        }
    }

    private static void checkSingleAnnotationManifests(String fileName, UnknownDocument unknownDocument,
                                                       SignatureContent existingContent)
            throws EnvelopeMergingException, IOException {
        if (existingContent.getSingleAnnotationManifests().containsKey(fileName)) {
            SingleAnnotationManifest existingManifest =
                    existingContent.getSingleAnnotationManifests().get(fileName);
            if (!contentsMatch(existingManifest.getInputStream(), unknownDocument.getInputStream())) {
                throw new SingleAnnotationManifestMergingException(fileName);
            }
        }
    }

    private static void checkAnnotations(String fileName, UnknownDocument unknownDocument, SignatureContent content)
            throws EnvelopeMergingException, IOException {
        if (content.getAnnotations().containsKey(fileName)) {
            EnvelopeAnnotation currentAnnotation = content.getAnnotations().get(fileName);
            if (!contentsMatch(currentAnnotation.getInputStream(), unknownDocument.getInputStream())) {
                throw new EnvelopeAnnotationMergingException(fileName);
            }
        }
    }

    private static void checkDocuments(String fileName, EnvelopeDocument newDocument,
                                       Collection<? extends EnvelopeDocument> documents) throws EnvelopeMergingException {
        for (EnvelopeDocument doc : documents) {
            if (doc.getFileName().equals(fileName)) {
                for (HashAlgorithm algorithm : HashAlgorithm.getImplementedHashAlgorithms()) {
                    if(algorithm.isDeprecated(new Date())) {
                        continue;
                    }
                    try {
                        if (!newDocument.getDataHash(algorithm).equals(doc.getDataHash(algorithm))) {
                            throw new DocumentMergingException(fileName);
                        }
                    } catch (DataHashException e) {
                        // ignore since it is an EmptyEnvelopeDocument that can't generate new hash
                    }
                }
            }
        }
    }

    private static void verifySameManifestType(SignatureContent content, SignatureContent existingSignatureContent)
            throws EnvelopeMergingException {
        ManifestFactoryType manifestType = existingSignatureContent.getManifest().getRight().getManifestFactoryType();
        ManifestFactoryType newManifestFactoryType = content.getManifest().getRight().getManifestFactoryType();
        if (!manifestType.equals(newManifestFactoryType)) {
            throw new ManifestMergingException("New SignatureContent has different manifest type!");
        }
    }

    private static void verifySameSignatureType(SignatureContent content, SignatureContent existingSignatureContent)
            throws EnvelopeMergingException {
        String signatureType = existingSignatureContent.getManifest().getRight().getSignatureReference().getType();
        String newSignatureType = content.getManifest().getRight().getSignatureReference().getType();
        if (!signatureType.equals(newSignatureType)) {
            throw new SignatureMergingException("New SignatureContent has different signature type!");
        }
    }

    private static void verifyNonClashingManifests(SignatureContent content, SignatureContent existingContent)
            throws EnvelopeMergingException, IOException {
        Pair<String, Manifest> newManifest = content.getManifest();
        Pair<String, Manifest> currentManifest = existingContent.getManifest();
        if (currentManifest.getLeft().equals(newManifest.getLeft()) &&
                !contentsMatch(currentManifest.getRight().getInputStream(), newManifest.getRight().getInputStream())
                ) {
            throw new ManifestMergingException("New SignatureContent has clashing Manifest! Path: " + newManifest.getLeft());
        }
    }

    private static void verifyNonClashingDocumentsManifests(SignatureContent content, SignatureContent existingContent)
            throws EnvelopeMergingException, IOException {
        Pair<String, DocumentsManifest> newDocumentsManifest = content.getDocumentsManifest();
        Pair<String, DocumentsManifest> currentDocumentsManifest = existingContent.getDocumentsManifest();
        if (currentDocumentsManifest.getLeft().equals(newDocumentsManifest.getLeft()) &&
                !contentsMatch(
                        currentDocumentsManifest.getRight().getInputStream(),
                        newDocumentsManifest.getRight().getInputStream()
                )
                ) {
            throw new DocumentsManifestMergingException(newDocumentsManifest.getLeft());
        }
    }

    private static void verifyNonClashingAnnotationsManifests(SignatureContent content, SignatureContent existingContent)
            throws EnvelopeMergingException, IOException {
        Pair<String, AnnotationsManifest> newAnnotationsManifest = content.getAnnotationsManifest();
        Pair<String, AnnotationsManifest> currentAnnotationsManifest = existingContent.getAnnotationsManifest();
        if (currentAnnotationsManifest.getLeft().equals(newAnnotationsManifest.getLeft()) &&
                !contentsMatch(
                        currentAnnotationsManifest.getRight().getInputStream(),
                        newAnnotationsManifest.getRight().getInputStream()
                )
                ) {
            throw new AnnotationsManifestMergingException(newAnnotationsManifest.getLeft());
        }
    }

    private static void verifyNonClashingSignatures(SignatureContent content, SignatureContent existingContent)
            throws EnvelopeMergingException, IOException {
        String newSignaturePath = content.getManifest().getRight().getSignatureReference().getUri();
        String currentSignaturePath = existingContent.getManifest().getRight().getSignatureReference().getUri();
        EnvelopeSignature newSignature = content.getEnvelopeSignature();
        EnvelopeSignature currentSignature = existingContent.getEnvelopeSignature();
        if (currentSignaturePath.equals(newSignaturePath) &&
                !contentsMatch(currentSignature, newSignature)
                ) {
            throw new SignatureMergingException("New SignatureContent has clashing signature! Path: " + newSignaturePath);
        }
    }

    private static void verifyNonClashingSingleAnnotationManifests(SignatureContent content, SignatureContent existingContent)
            throws EnvelopeMergingException, IOException {
        for (String singleAnnotationManifestPath : existingContent.getSingleAnnotationManifests().keySet()) {
            if (content.getSingleAnnotationManifests().containsKey(singleAnnotationManifestPath)) {
                SingleAnnotationManifest existingManifest =
                        existingContent.getSingleAnnotationManifests().get(singleAnnotationManifestPath);
                SingleAnnotationManifest newManifest = content.getSingleAnnotationManifests().get(singleAnnotationManifestPath);
                if (!contentsMatch(existingManifest.getInputStream(), newManifest.getInputStream())) {
                    throw new SingleAnnotationManifestMergingException(singleAnnotationManifestPath);
                }
            }
        }
    }

    private static void verifyNonClashingAnnotations(SignatureContent content, SignatureContent existingContent)
            throws EnvelopeMergingException, IOException {
        for (String annotationPath : existingContent.getAnnotations().keySet()) {
            if (content.getAnnotations().containsKey(annotationPath)) {
                EnvelopeAnnotation newAnnotation = content.getAnnotations().get(annotationPath);
                EnvelopeAnnotation currentAnnotation = existingContent.getAnnotations().get(annotationPath);
                if (!contentsMatch(currentAnnotation.getInputStream(), newAnnotation.getInputStream())) {
                    throw new EnvelopeAnnotationMergingException(annotationPath);
                }
            }
        }
    }

    private static void verifyNonClashingEnvelopeDocuments(SignatureContent content, SignatureContent existingContent)
            throws EnvelopeMergingException, IOException {
        for (Map.Entry<String, EnvelopeDocument> newDocument : content.getDocuments().entrySet()) {
            checkDocuments(newDocument.getKey(), newDocument.getValue(), existingContent.getDocuments().values());
        }
    }

    private static boolean contentsMatch(InputStream firstStream, InputStream secondStream) throws IOException {
        try {
            DataHash first = new DataHasher().addData(firstStream).getHash();
            DataHash second = new DataHasher().addData(secondStream).getHash();
            return first.equals(second);
        } finally {
            firstStream.close();
            secondStream.close();
        }
    }

    private static boolean contentsMatch(EnvelopeSignature first, EnvelopeSignature second) throws IOException {
        return contentsMatch(new ByteArrayInputStream(toByteArray(first)), new ByteArrayInputStream(toByteArray(second)));
    }

    private static byte[] toByteArray(EnvelopeSignature signature) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            signature.writeTo(bos);
            return bos.toByteArray();
        }
    }

}
