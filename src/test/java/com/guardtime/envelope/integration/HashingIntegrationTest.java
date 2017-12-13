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

package com.guardtime.envelope.integration;

import com.guardtime.envelope.EnvelopeBuilder;
import com.guardtime.envelope.annotation.EnvelopeAnnotation;
import com.guardtime.envelope.annotation.EnvelopeAnnotationType;
import com.guardtime.envelope.annotation.StringEnvelopeAnnotation;
import com.guardtime.envelope.document.EnvelopeDocument;
import com.guardtime.envelope.document.StreamEnvelopeDocument;
import com.guardtime.envelope.hash.HashAlgorithmProvider;
import com.guardtime.envelope.manifest.EnvelopeManifestFactory;
import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.manifest.SingleAnnotationManifest;
import com.guardtime.envelope.manifest.tlv.TlvEnvelopeManifestFactory;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopePackagingFactory;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.packaging.zip.ZipEnvelopePackagingFactoryBuilder;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.unisignature.KSISignature;
import com.guardtime.util.TestHashAlgorithmProvider;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HashingIntegrationTest extends AbstractCommonIntegrationTest {

    private static final String ENVELOPE_ANNOTATION_TYPE_DOMAIN = "Some kind of domain";
    private static final String ENVELOPE_ANNOTATION_CONTENT = "StringEnvelopeAnnotationTypeIsFullyRemovable";
    private static final String ENVELOPE_DOCUMENT_FILE_NAME = "StreamFile.txt";
    private static final String ENVELOPE_DOCUMENT_MIME_TYPE = "Stream";
    private static final String INPUT_STREAM_STRING = "Input from stream.";
    private final EnvelopeAnnotation ENVELOPE_ANNOTATION = new StringEnvelopeAnnotation(EnvelopeAnnotationType.FULLY_REMOVABLE, ENVELOPE_ANNOTATION_CONTENT, ENVELOPE_ANNOTATION_TYPE_DOMAIN);
    private final EnvelopeDocument ENVELOPE_DOCUMENT = new StreamEnvelopeDocument(new ByteArrayInputStream(INPUT_STREAM_STRING.getBytes(StandardCharsets.UTF_8)), ENVELOPE_DOCUMENT_MIME_TYPE, ENVELOPE_DOCUMENT_FILE_NAME);
    private Envelope envelope;

    @After
    public void cleanUp() throws Exception {
        ENVELOPE_ANNOTATION.close();
        ENVELOPE_DOCUMENT.close();
        if (envelope != null) {
            envelope.close();
        }
    }

    @Test
    public void testDocumentHashAlgorithmListIsEmpty_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Data hashes must not be empty");

        HashAlgorithm hashingAlgorithm = HashAlgorithm.SHA2_256;
        LinkedList<HashAlgorithm> list = new LinkedList<>();
        HashAlgorithmProvider provider = new TestHashAlgorithmProvider(Collections.singletonList(hashingAlgorithm), list, hashingAlgorithm, hashingAlgorithm);
        setUpEnvelope(provider);
    }

    @Test
    public void testFileReferenceHashAlgorithmListIsEmpty_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Data hashes must not be empty");

        HashAlgorithm hashingAlgorithm = HashAlgorithm.SHA2_256;
        LinkedList<HashAlgorithm> list = new LinkedList<>();
        HashAlgorithmProvider provider = new TestHashAlgorithmProvider(list, Collections.singletonList(hashingAlgorithm), hashingAlgorithm, hashingAlgorithm);
        setUpEnvelope(provider);
    }

    @Test
    public void testFileReferenceHashAlgorithmListContainsNullElement_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Hash algorithm can not be null");

        HashAlgorithm hashingAlgorithm = HashAlgorithm.SHA2_256;
        LinkedList<HashAlgorithm> list = new LinkedList<>();
        list.add(hashingAlgorithm);
        list.add(null);
        HashAlgorithmProvider provider = new TestHashAlgorithmProvider(list, Collections.singletonList(hashingAlgorithm), hashingAlgorithm, hashingAlgorithm);
        setUpEnvelope(provider);
    }

    @Test
    public void testDocumentHashAlgorithmListContainsNullElement_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Hash algorithm can not be null");

        HashAlgorithm hashingAlgorithm = HashAlgorithm.SHA2_256;
        LinkedList<HashAlgorithm> list = new LinkedList<>();
        list.add(hashingAlgorithm);
        list.add(null);
        HashAlgorithmProvider provider = new TestHashAlgorithmProvider(list, Collections.singletonList(hashingAlgorithm), hashingAlgorithm, hashingAlgorithm);
        setUpEnvelope(provider);
    }

    @Test
    public void testDocumentHashAlgorithmListIsNull_NullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Hash algorithm list must be present");

        HashAlgorithm hashingAlgorithm = HashAlgorithm.SHA2_256;
        HashAlgorithmProvider provider = new TestHashAlgorithmProvider(Collections.singletonList(hashingAlgorithm), null, hashingAlgorithm, hashingAlgorithm);
        setUpEnvelope(provider);
    }

    @Test
    public void testFileReferenceHashAlgorithmListIsNull_NullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Hash algorithm list must be present");

        HashAlgorithm hashingAlgorithm = HashAlgorithm.SHA2_256;
        HashAlgorithmProvider provider = new TestHashAlgorithmProvider(null, Collections.singletonList(hashingAlgorithm), hashingAlgorithm, hashingAlgorithm);
        setUpEnvelope(provider);
    }

    @Test
    public void testAnnotationDataReferenceAlgorithmIsNull_NullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Hash algorithm can not be null");

        HashAlgorithm hashingAlgorithm = HashAlgorithm.SHA2_256;
        HashAlgorithmProvider provider = new TestHashAlgorithmProvider(Collections.singletonList(hashingAlgorithm), Collections.singletonList(hashingAlgorithm), null, hashingAlgorithm);
        setUpEnvelope(provider);
    }

    @Test
    public void testSigningHashAlgorithmIsNull_NullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Hash algorithm can not be null");

        HashAlgorithm hashingAlgorithm = HashAlgorithm.SHA2_256;
        HashAlgorithmProvider provider = new TestHashAlgorithmProvider(Collections.singletonList(hashingAlgorithm), Collections.singletonList(hashingAlgorithm), hashingAlgorithm, null);
        setUpEnvelope(provider);
    }

    @Test
    public void testCheckNonDefaultHashingAlgorithm() throws Exception {
        HashAlgorithm hashingAlgorithm = HashAlgorithm.SHA2_512;
        HashAlgorithmProvider provider = new TestHashAlgorithmProvider(hashingAlgorithm);
        setUpEnvelope(provider);

        SignatureContent signatureContent = envelope.getSignatureContents().get(0);

        Manifest manifest = signatureContent.getManifest().getRight();
        checkDataHashList(hashingAlgorithm, manifest.getDocumentsManifestReference().getHashList());
        checkDataHashList(hashingAlgorithm, manifest.getAnnotationsManifestReference().getHashList());
        checkDataHashList(hashingAlgorithm, signatureContent.getDocumentsManifest().getRight().getDocumentReferences().get(0).getHashList());
        checkDataHashList(hashingAlgorithm, signatureContent.getAnnotationsManifest().getRight().getSingleAnnotationManifestReferences().get(0).getHashList());
        Map<String, SingleAnnotationManifest> singleAnnotationManifestMap = signatureContent.getSingleAnnotationManifests();
        for (String key : singleAnnotationManifestMap.keySet()) {
            checkDataHashList(hashingAlgorithm, singleAnnotationManifestMap.get(key).getDocumentsManifestReference().getHashList());
            Assert.assertEquals(hashingAlgorithm, singleAnnotationManifestMap.get(key).getAnnotationReference().getHash().getAlgorithm());
        }
        KSISignature signature = (KSISignature) signatureContent.getEnvelopeSignature().getSignature();
        Assert.assertEquals(hashingAlgorithm, signature.getInputHash().getAlgorithm());
    }

    @Test
    public void testUseSeveralHashingAlgorithms() throws Exception {
        List<HashAlgorithm> hashes = Arrays.asList(HashAlgorithm.SHA1,
                HashAlgorithm.RIPEMD_160,
                HashAlgorithm.SHA2_256,
                HashAlgorithm.SHA2_384,
                HashAlgorithm.SHA2_512);
        HashAlgorithmProvider provider = new TestHashAlgorithmProvider(
                hashes, hashes, HashAlgorithm.SHA2_256, HashAlgorithm.SHA2_256);

        setUpEnvelope(provider);
        SignatureContent signatureContent = envelope.getSignatureContents().get(0);
        Manifest manifest = signatureContent.getManifest().getRight();
        Collection<SingleAnnotationManifest> singleAnnotationManifestValues = signatureContent.getSingleAnnotationManifests().values();

        checkDataHashList(hashes, manifest.getDocumentsManifestReference().getHashList());
        checkDataHashList(hashes, manifest.getAnnotationsManifestReference().getHashList());
        checkDataHashList(hashes, signatureContent.getDocumentsManifest().getRight().getDocumentReferences().get(0).getHashList());
        checkDataHashList(hashes, signatureContent.getAnnotationsManifest().getRight().getSingleAnnotationManifestReferences().get(0).getHashList());
        for (SingleAnnotationManifest value : singleAnnotationManifestValues) {
            checkDataHashList(hashes, value.getDocumentsManifestReference().getHashList());
            Assert.assertEquals(HashAlgorithm.SHA2_256, value.getAnnotationReference().getHash().getAlgorithm());
        }
        KSISignature signature = (KSISignature) signatureContent.getEnvelopeSignature().getSignature();
        Assert.assertEquals(HashAlgorithm.SHA2_256, signature.getInputHash().getAlgorithm());
    }

    @Test
    public void testDifferentHashingAlgorithmsForDifferentParts() throws Exception {
        List<HashAlgorithm> fileReferenceHashAlgorithms = Collections.singletonList(HashAlgorithm.RIPEMD_160);
        List<HashAlgorithm> documentReferenceHashAlgorithms = Collections.singletonList(HashAlgorithm.SHA1);
        HashAlgorithm annotationDataReferenceHashAlgorithm = HashAlgorithm.SHA2_384;
        HashAlgorithm signingHashAlgorithm = HashAlgorithm.SHA2_512;
        HashAlgorithmProvider provider = new TestHashAlgorithmProvider(
                fileReferenceHashAlgorithms,
                documentReferenceHashAlgorithms,
                annotationDataReferenceHashAlgorithm,
                signingHashAlgorithm);

        setUpEnvelope(provider);
        SignatureContent signatureContent = envelope.getSignatureContents().get(0);
        Manifest manifest = signatureContent.getManifest().getRight();
        Collection<SingleAnnotationManifest> singleAnnotationManifestValues = signatureContent.getSingleAnnotationManifests().values();

        checkDataHashList(fileReferenceHashAlgorithms, manifest.getDocumentsManifestReference().getHashList());
        checkDataHashList(fileReferenceHashAlgorithms, manifest.getAnnotationsManifestReference().getHashList());
        checkDataHashList(documentReferenceHashAlgorithms, signatureContent.getDocumentsManifest().getRight().getDocumentReferences().get(0).getHashList());
        checkDataHashList(fileReferenceHashAlgorithms, signatureContent.getAnnotationsManifest().getRight().getSingleAnnotationManifestReferences().get(0).getHashList());
        for (SingleAnnotationManifest value : singleAnnotationManifestValues) {
            checkDataHashList(fileReferenceHashAlgorithms, value.getDocumentsManifestReference().getHashList());
            Assert.assertEquals(annotationDataReferenceHashAlgorithm, value.getAnnotationReference().getHash().getAlgorithm());
        }
        KSISignature signature = (KSISignature) signatureContent.getEnvelopeSignature().getSignature();
        Assert.assertEquals(signingHashAlgorithm, signature.getInputHash().getAlgorithm());
    }

    @Test
    public void testUsingNotImplementedHashingAlgorithm_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Hash algorithm SHA3_512 is not implemented");
        HashAlgorithm hashAlgorithm = HashAlgorithm.SHA3_512;
        HashAlgorithmProvider hashAlgorithmProvider = new TestHashAlgorithmProvider(hashAlgorithm);
        EnvelopeBuilder builder = new EnvelopeBuilder(getEnvelopePackagingFactory(hashAlgorithmProvider));
        builder.withAnnotation(ENVELOPE_ANNOTATION);
        builder.withDocument(ENVELOPE_DOCUMENT);
        builder.build();
    }

    @Test
     public void testUsingNotImplementedHashingAlgorithmInList_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Hash algorithm SM3 is not implemented");
        List<HashAlgorithm> hashAlgorithmList = Arrays.asList(HashAlgorithm.SHA1, HashAlgorithm.SHA2_256, HashAlgorithm.SM3);
        HashAlgorithm hashAlgorithm = HashAlgorithm.SHA2_512;
        HashAlgorithmProvider hashAlgorithmProvider = new TestHashAlgorithmProvider(hashAlgorithmList, hashAlgorithmList, hashAlgorithm, hashAlgorithm);
        EnvelopeBuilder builder = new EnvelopeBuilder(getEnvelopePackagingFactory(hashAlgorithmProvider));
        builder.withAnnotation(ENVELOPE_ANNOTATION);
        builder.withDocument(ENVELOPE_DOCUMENT);
        builder.build();
    }

    private EnvelopePackagingFactory getEnvelopePackagingFactory(HashAlgorithmProvider provider) throws Exception {
        EnvelopeManifestFactory envelopeManifestFactory = new TlvEnvelopeManifestFactory(provider);
        return new ZipEnvelopePackagingFactoryBuilder().
                withSignatureFactory(signatureFactory).
                withManifestFactory(envelopeManifestFactory).
                build();
    }

    private void setUpEnvelope(HashAlgorithmProvider provider) throws Exception {
        EnvelopeBuilder builder = new EnvelopeBuilder(getEnvelopePackagingFactory(provider));
        builder.withAnnotation(ENVELOPE_ANNOTATION);
        builder.withDocument(ENVELOPE_DOCUMENT);
        this.envelope = builder.build();
    }

    private void checkDataHashList(List<HashAlgorithm> expectedHashAlgorithms, List<DataHash> dataHashes) throws Exception {
        Assert.assertEquals(expectedHashAlgorithms.size(), dataHashes.size());
        List<HashAlgorithm> foundAlgorithms = new LinkedList<>();
        for (DataHash dataHash : dataHashes) {
            foundAlgorithms.add(dataHash.getAlgorithm());
        }
        for (HashAlgorithm expectedAlgorithm : expectedHashAlgorithms) {
            Assert.assertTrue(String.format("Expected hash with algorithm %s was not found.", expectedAlgorithm), foundAlgorithms.contains(expectedAlgorithm));
        }
    }

    private void checkDataHashList(HashAlgorithm expectedHashAlgorithm, List<DataHash> dataHashes) throws Exception {
        checkDataHashList(Collections.singletonList(expectedHashAlgorithm), dataHashes);
    }
}
