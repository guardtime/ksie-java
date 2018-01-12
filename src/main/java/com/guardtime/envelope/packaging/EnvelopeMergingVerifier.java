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

import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.document.Document;
import com.guardtime.envelope.document.UnknownDocument;
import com.guardtime.envelope.manifest.AnnotationsManifest;
import com.guardtime.envelope.manifest.DocumentsManifest;
import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.manifest.ManifestFactoryType;
import com.guardtime.envelope.manifest.SingleAnnotationManifest;
import com.guardtime.envelope.packaging.exception.AnnotationMergingException;
import com.guardtime.envelope.packaging.exception.AnnotationsManifestMergingException;
import com.guardtime.envelope.packaging.exception.DocumentMergingException;
import com.guardtime.envelope.packaging.exception.DocumentsManifestMergingException;
import com.guardtime.envelope.packaging.exception.EnvelopeMergingException;
import com.guardtime.envelope.packaging.exception.ManifestMergingException;
import com.guardtime.envelope.packaging.exception.SignatureMergingException;
import com.guardtime.envelope.packaging.exception.SingleAnnotationManifestMergingException;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.util.DataHashException;
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

class EnvelopeMergingVerifier {

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
                verifyNonClashingDocuments(newContent, existingContent);
            }
        } catch (IOException e) {
            throw new EnvelopeMergingException("Failed to verify uniqueness!", e);
        }
    }

    public static void verifyUniqueUnknownFiles(Envelope newEnvelope, Envelope existingEnvelope)
            throws EnvelopeMergingException {
        try {
            for (UnknownDocument unknownDocument : newEnvelope.getUnknownFiles()) {
                checkUniqueness(unknownDocument.getPath(), unknownDocument, existingEnvelope);
            }

            for (UnknownDocument unknownDocument : existingEnvelope.getUnknownFiles()) {
                checkUniqueness(unknownDocument.getPath(), unknownDocument, newEnvelope);
            }
        } catch (IOException e) {
            throw new EnvelopeMergingException("Failed to verify uniqueness!", e);
        }
    }

    private static void checkUniqueness(String path, UnknownDocument unknownDocument, Envelope existingEnvelope)
            throws EnvelopeMergingException, IOException {
        for (SignatureContent content : existingEnvelope.getSignatureContents()) {
            checkDocuments(path, unknownDocument, content.getDocuments().values());
            checkAnnotations(path, unknownDocument, content);
            checkManifests(path, unknownDocument, content);
            checkSignature(path, unknownDocument, content);
        }
        checkDocuments(path, unknownDocument, existingEnvelope.getUnknownFiles());
    }

    private static void checkSignature(String path, UnknownDocument unknownDocument, SignatureContent content)
            throws EnvelopeMergingException, IOException {
        String existingSignatureUri = content.getManifest().getSignatureReference().getUri();
        if (existingSignatureUri.equals(path)) {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(toByteArray(content.getEnvelopeSignature()))) {
                if (!contentsMatch(bis, unknownDocument.getInputStream())) {
                    throw new SignatureMergingException("New SignatureContent has clashing signature! Path: " + path);
                }
            }
        }
    }

    private static void checkManifests(String path, UnknownDocument unknownDocument, SignatureContent content)
            throws EnvelopeMergingException, IOException {
        if (content.getManifest().getPath().equals(path)) {
            if (!contentsMatch(content.getManifest().getInputStream(), unknownDocument.getInputStream())
                    ) {
                throw new ManifestMergingException("New SignatureContent has clashing Manifest! Path: " + path);
            }
        } else if (content.getDocumentsManifest().getPath().equals(path)) {
            if (!contentsMatch(content.getDocumentsManifest().getInputStream(), unknownDocument.getInputStream())) {
                throw new DocumentsManifestMergingException(path);
            }
        } else if (content.getAnnotationsManifest().getPath().equals(path)) {
            if (!contentsMatch(content.getAnnotationsManifest().getInputStream(), unknownDocument.getInputStream())) {
                throw new AnnotationsManifestMergingException(path);
            }
        } else {
            checkSingleAnnotationManifests(path, unknownDocument, content);
        }
    }

    private static void checkSingleAnnotationManifests(String path, UnknownDocument unknownDocument,
                                                       SignatureContent existingContent)
            throws EnvelopeMergingException, IOException {
        if (existingContent.getSingleAnnotationManifests().containsKey(path)) {
            SingleAnnotationManifest existingManifest =
                    existingContent.getSingleAnnotationManifests().get(path);
            if (!contentsMatch(existingManifest.getInputStream(), unknownDocument.getInputStream())) {
                throw new SingleAnnotationManifestMergingException(path);
            }
        }
    }

    private static void checkAnnotations(String path, UnknownDocument unknownDocument, SignatureContent content)
            throws EnvelopeMergingException, IOException {
        if (content.getAnnotations().containsKey(path)) {
            Annotation currentAnnotation = content.getAnnotations().get(path);
            if (!contentsMatch(currentAnnotation.getInputStream(), unknownDocument.getInputStream())) {
                throw new AnnotationMergingException(path);
            }
        }
    }

    private static void checkDocuments(String path, Document newDocument,
                                       Collection<? extends Document> documents)
            throws EnvelopeMergingException {
        for (Document doc : documents) {
            if (doc.getPath().equals(path)) {
                for (HashAlgorithm algorithm : HashAlgorithm.getImplementedHashAlgorithms()) {
                    if (algorithm.isDeprecated(new Date())) {
                        continue;
                    }
                    try {
                        if (!newDocument.getDataHash(algorithm).equals(doc.getDataHash(algorithm))) {
                            throw new DocumentMergingException(path);
                        }
                    } catch (DataHashException e) {
                        // ignore since it is an EmptyDocument that can't generate new hash
                    }
                }
            }
        }
    }

    private static void verifySameManifestType(SignatureContent content, SignatureContent existingSignatureContent)
            throws EnvelopeMergingException {
        ManifestFactoryType manifestType = existingSignatureContent.getManifest().getManifestFactoryType();
        ManifestFactoryType newManifestFactoryType = content.getManifest().getManifestFactoryType();
        if (!manifestType.equals(newManifestFactoryType)) {
            throw new ManifestMergingException("New SignatureContent has different manifest type!");
        }
    }

    private static void verifySameSignatureType(SignatureContent content, SignatureContent existingSignatureContent)
            throws EnvelopeMergingException {
        String signatureType = existingSignatureContent.getManifest().getSignatureReference().getType();
        String newSignatureType = content.getManifest().getSignatureReference().getType();
        if (!signatureType.equals(newSignatureType)) {
            throw new SignatureMergingException("New SignatureContent has different signature type!");
        }
    }

    private static void verifyNonClashingManifests(SignatureContent content, SignatureContent existingContent)
            throws EnvelopeMergingException, IOException {
        Manifest newManifest = content.getManifest();
        Manifest currentManifest = existingContent.getManifest();
        if (currentManifest.getPath().equals(newManifest.getPath()) &&
                !contentsMatch(currentManifest.getInputStream(), newManifest.getInputStream())
                ) {
            throw new ManifestMergingException("New SignatureContent has clashing Manifest! Path: " + newManifest.getPath());
        }
    }

    private static void verifyNonClashingDocumentsManifests(SignatureContent content, SignatureContent existingContent)
            throws EnvelopeMergingException, IOException {
        DocumentsManifest newDocumentsManifest = content.getDocumentsManifest();
        DocumentsManifest currentDocumentsManifest = existingContent.getDocumentsManifest();
        if (currentDocumentsManifest.getPath().equals(newDocumentsManifest.getPath()) &&
                !contentsMatch(
                        currentDocumentsManifest.getInputStream(),
                        newDocumentsManifest.getInputStream()
                )
                ) {
            throw new DocumentsManifestMergingException(newDocumentsManifest.getPath());
        }
    }

    private static void verifyNonClashingAnnotationsManifests(SignatureContent content, SignatureContent existingContent)
            throws EnvelopeMergingException, IOException {
        AnnotationsManifest newAnnotationsManifest = content.getAnnotationsManifest();
        AnnotationsManifest currentAnnotationsManifest = existingContent.getAnnotationsManifest();
        if (currentAnnotationsManifest.getPath().equals(newAnnotationsManifest.getPath()) &&
                !contentsMatch(
                        currentAnnotationsManifest.getInputStream(),
                        newAnnotationsManifest.getInputStream()
                )
                ) {
            throw new AnnotationsManifestMergingException(newAnnotationsManifest.getPath());
        }
    }

    private static void verifyNonClashingSignatures(SignatureContent content, SignatureContent existingContent)
            throws EnvelopeMergingException, IOException {
        String newSignaturePath = content.getManifest().getSignatureReference().getUri();
        String currentSignaturePath = existingContent.getManifest().getSignatureReference().getUri();
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
                Annotation newAnnotation = content.getAnnotations().get(annotationPath);
                Annotation currentAnnotation = existingContent.getAnnotations().get(annotationPath);
                if (!contentsMatch(currentAnnotation.getInputStream(), newAnnotation.getInputStream())) {
                    throw new AnnotationMergingException(annotationPath);
                }
            }
        }
    }

    private static void verifyNonClashingDocuments(SignatureContent content, SignatureContent existingContent)
            throws EnvelopeMergingException {
        for (Map.Entry<String, Document> newDocument : content.getDocuments().entrySet()) {
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
