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

package com.guardtime.envelope.packaging.zip;

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.envelope.annotation.EnvelopeAnnotation;
import com.guardtime.envelope.document.EnvelopeDocument;
import com.guardtime.envelope.document.StreamEnvelopeDocument;
import com.guardtime.envelope.indexing.IncrementingIndexProviderFactory;
import com.guardtime.envelope.indexing.UuidIndexProviderFactory;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopePackagingFactory;
import com.guardtime.envelope.packaging.exception.EnvelopeMergingException;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class ZipEnvelopeTest extends AbstractEnvelopeTest {

    @Test
    public void testAddSingleSignatureContent_OK() throws Exception {
        EnvelopePackagingFactory packagingFactory = new ZipEnvelopePackagingFactoryBuilder().
                withSignatureFactory(mockedSignatureFactory).
                disableInternalVerification().
                withIndexProviderFactory(new UuidIndexProviderFactory()).
                build();
        try (Envelope envelope = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_PDF), singletonList(STRING_ENVELOPE_ANNOTATION))) {
            assertEquals(1, envelope.getSignatureContents().size());
            try (Envelope newEnvelope = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_TEXT), new ArrayList<EnvelopeAnnotation>())) {
                envelope.add(newEnvelope.getSignatureContents().get(0));
                assertEquals(2, envelope.getSignatureContents().size());
            }
        }
    }

    @Test
    public void testAddEnvelope_OK() throws Exception {
        EnvelopePackagingFactory packagingFactory = new ZipEnvelopePackagingFactoryBuilder().
                withSignatureFactory(mockedSignatureFactory).
                disableInternalVerification().
                withIndexProviderFactory(new UuidIndexProviderFactory()).
                build();
        try (Envelope envelope = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_PDF), singletonList(STRING_ENVELOPE_ANNOTATION))) {
            assertEquals(1, envelope.getSignatureContents().size());
            try (Envelope newEnvelope = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_TEXT), new ArrayList<EnvelopeAnnotation>())) {
                envelope.add(newEnvelope);
                assertEquals(2, envelope.getSignatureContents().size());
            }
        }
    }

    @Test
    public void testAddListOfSignatureContent_OK() throws Exception {
        EnvelopePackagingFactory packagingFactory = new ZipEnvelopePackagingFactoryBuilder().
                withSignatureFactory(mockedSignatureFactory).
                disableInternalVerification().
                withIndexProviderFactory(new UuidIndexProviderFactory()).
                build();
        try (Envelope envelope = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_PDF), singletonList(STRING_ENVELOPE_ANNOTATION))) {
            assertEquals(1, envelope.getSignatureContents().size());
            try (Envelope newEnvelope = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_TEXT), new ArrayList<EnvelopeAnnotation>());
                 ByteArrayInputStream input = new ByteArrayInputStream("auh".getBytes(StandardCharsets.UTF_8));
                 EnvelopeDocument envelopeDocument = new StreamEnvelopeDocument(input, "text/plain", "someTestFile.txt")
            ) {
                packagingFactory.addSignature(newEnvelope, singletonList(envelopeDocument), new ArrayList<EnvelopeAnnotation>());
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
        EnvelopePackagingFactory packagingFactory = new ZipEnvelopePackagingFactoryBuilder().
                withSignatureFactory(mockedSignatureFactory).
                disableInternalVerification().
                withIndexProviderFactory(new IncrementingIndexProviderFactory()).
                build();
        try (Envelope envelope = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_PDF), singletonList(STRING_ENVELOPE_ANNOTATION))) {
            assertEquals(1, envelope.getSignatureContents().size());
            try (Envelope newEnvelope = packagingFactory.create(singletonList(TEST_DOCUMENT_HELLO_TEXT), new ArrayList<EnvelopeAnnotation>())) {
                envelope.add(newEnvelope.getSignatureContents().get(0));
            }
        }
    }

    @Test
    public void testAddWithSameEnvelopeDocumentPath_ThrowsEnvelopeMergingException() throws Exception {
        expectedException.expect(EnvelopeMergingException.class);
        expectedException.expectMessage("New SignatureContent has clashing name for EnvelopeDocument! Path: ");
        EnvelopePackagingFactory packagingFactory = new ZipEnvelopePackagingFactoryBuilder().
                withSignatureFactory(mockedSignatureFactory).
                disableInternalVerification().
                withIndexProviderFactory(new UuidIndexProviderFactory()).
                build();
        try (Envelope envelope = packagingFactory.create(
                singletonList(TEST_DOCUMENT_HELLO_PDF),
                singletonList(STRING_ENVELOPE_ANNOTATION)
        )) {
            assertEquals(1, envelope.getSignatureContents().size());
            try (EnvelopeDocument clashingDocument = new StreamEnvelopeDocument(
                    new ByteArrayInputStream(TEST_DATA_TXT_CONTENT),
                    TEST_DOCUMENT_HELLO_PDF.getMimeType(),
                    TEST_DOCUMENT_HELLO_PDF.getFileName()
            );
                 Envelope newEnvelope = packagingFactory.create(singletonList(clashingDocument), new ArrayList<EnvelopeAnnotation>())
            ) {
                envelope.add(newEnvelope.getSignatureContents().get(0));
            }
        }
    }
}
