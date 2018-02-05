/*
 * Copyright 2013-2018 Guardtime, Inc.
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

package com.guardtime.envelope.packaging.zip;

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.document.Document;
import com.guardtime.envelope.document.StreamDocument;
import com.guardtime.envelope.indexing.IncrementingIndexProviderFactory;
import com.guardtime.envelope.indexing.UuidIndexProviderFactory;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopePackagingFactory;
import com.guardtime.envelope.packaging.exception.EnvelopeMergingException;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.ksi.hashing.DataHash;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static com.guardtime.envelope.packaging.EntryNameProvider.META_INF;
import static com.guardtime.envelope.packaging.EnvelopeWriter.MIME_TYPE_ENTRY_NAME;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ZipEnvelopeTest extends AbstractEnvelopeTest {

    @Before
    public void setUpSignatureFactory() throws Exception {
        EnvelopeSignature mockSignature = mock(EnvelopeSignature.class);
        when(mockedSignatureFactory.create(any(DataHash.class))).thenReturn(mockSignature);
    }

    @Test
    public void testAddSingleSignatureContent_OK() throws Exception {
        EnvelopePackagingFactory packagingFactory = new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(mockedSignatureFactory)
                .withVerificationPolicy(null)
                .withIndexProviderFactory(new UuidIndexProviderFactory())
                .build();
        try (
                Envelope envelope =
                     packagingFactory.create(singletonList(testDocumentHelloPdf), singletonList(stringEnvelopeAnnotation))
        ) {
            assertEquals(1, envelope.getSignatureContents().size());
            try (
                    Envelope newEnvelope =
                            packagingFactory.create(singletonList(testDocumentHelloText), new ArrayList<Annotation>())
            ) {
                envelope.add(newEnvelope.getSignatureContents().get(0));
                assertEquals(2, envelope.getSignatureContents().size());
            }
        }
    }

    @Test
    public void testAddEnvelope_OK() throws Exception {
        EnvelopePackagingFactory packagingFactory = new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(mockedSignatureFactory)
                .withVerificationPolicy(null)
                .withIndexProviderFactory(new UuidIndexProviderFactory())
                .build();
        try (
                Envelope envelope =
                        packagingFactory.create(singletonList(testDocumentHelloPdf), singletonList(stringEnvelopeAnnotation))
        ) {
            assertEquals(1, envelope.getSignatureContents().size());
            try (
                    Envelope newEnvelope =
                            packagingFactory.create(singletonList(testDocumentHelloText), new ArrayList<Annotation>())
            ) {
                envelope.add(newEnvelope);
                assertEquals(2, envelope.getSignatureContents().size());
            }
        }
    }

    @Test
    public void testAddListOfSignatureContent_OK() throws Exception {
        EnvelopePackagingFactory packagingFactory = new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(mockedSignatureFactory)
                .withVerificationPolicy(null)
                .withIndexProviderFactory(new UuidIndexProviderFactory())
                .build();
        try (
                Envelope envelope =
                        packagingFactory.create(singletonList(testDocumentHelloPdf), singletonList(stringEnvelopeAnnotation))
        ) {
            assertEquals(1, envelope.getSignatureContents().size());
            try (
                    Envelope newEnvelope =
                            packagingFactory.create(singletonList(testDocumentHelloText), new ArrayList<Annotation>());
                    ByteArrayInputStream input = new ByteArrayInputStream("auh".getBytes(StandardCharsets.UTF_8));
                    Document document = new StreamDocument(input, "text/plain", "someTestFile.txt")
            ) {
                packagingFactory.addSignature(newEnvelope, singletonList(document), new ArrayList<Annotation>());
                int expected = newEnvelope.getSignatureContents().size() + 1;
                envelope.add(newEnvelope);
                assertEquals(expected, envelope.getSignatureContents().size());
            }
        }
    }

    @Test
    public void testAddWithSameManifestPath_ThrowsEnvelopeMergingException() throws Exception {
        expectedException.expect(EnvelopeMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing Manifest!");
        EnvelopePackagingFactory packagingFactory = new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(mockedSignatureFactory)
                .withVerificationPolicy(null)
                .withIndexProviderFactory(new IncrementingIndexProviderFactory())
                .build();
        try (
                Envelope envelope =
                        packagingFactory.create(singletonList(testDocumentHelloPdf), singletonList(stringEnvelopeAnnotation))
        ) {
            assertEquals(1, envelope.getSignatureContents().size());
            try (
                    Envelope newEnvelope =
                            packagingFactory.create(singletonList(testDocumentHelloText), new ArrayList<Annotation>())
            ) {
                envelope.add(newEnvelope.getSignatureContents().get(0));
            }
        }
    }

    @Test
    public void testAddWithSameDocumentPath_ThrowsEnvelopeMergingException() throws Exception {
        expectedException.expect(EnvelopeMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing name for Document! Path: ");
        EnvelopePackagingFactory packagingFactory = new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(mockedSignatureFactory)
                .withVerificationPolicy(null)
                .withIndexProviderFactory(new UuidIndexProviderFactory())
                .build();
        try (Envelope envelope = packagingFactory.create(
                singletonList(testDocumentHelloPdf),
                singletonList(stringEnvelopeAnnotation)
        )) {
            assertEquals(1, envelope.getSignatureContents().size());
            try (Document clashingDocument = new StreamDocument(
                    new ByteArrayInputStream(TEST_DATA_TXT_CONTENT),
                    testDocumentHelloPdf.getMimeType(),
                    testDocumentHelloPdf.getFileName()
            );
                 Envelope newEnvelope = packagingFactory.create(singletonList(clashingDocument), new ArrayList<Annotation>())
            ) {
                envelope.add(newEnvelope.getSignatureContents().get(0));
            }
        }
    }

    @Test
    public void testAddDocumentWithMIMEtypeName_ThrowsIOException() throws Exception {
        performFilenameTest(MIME_TYPE_ENTRY_NAME);
    }

    @Test
    public void testAddDocumentWithMETAINFDirInName_ThrowsIOException() throws Exception {
        performFilenameTest(META_INF + "/somefile.txt");
    }

    @Test
    public void testAddDocumentWithMETAINFName_ThrowsIOException() throws Exception {
        performFilenameTest(META_INF);
    }

    private void performFilenameTest(String filename) throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("File name is not valid!");
        EnvelopePackagingFactory packagingFactory = new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(mockedSignatureFactory)
                .withVerificationPolicy(null)
                .withIndexProviderFactory(new UuidIndexProviderFactory())
                .build();
        Document testDocument = new StreamDocument(
                new ByteArrayInputStream(new byte[0]),
                "some type",
                filename
        );
        try (Envelope ignored = packagingFactory.create(singletonList(testDocument), singletonList(stringEnvelopeAnnotation))) {
            //empty
        }
    }

}
