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

package com.guardtime.envelope.integration;

import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.annotation.AnnotationBuilder;
import com.guardtime.envelope.annotation.EnvelopeAnnotationType;
import com.guardtime.envelope.document.Document;
import com.guardtime.envelope.document.DocumentBuilder;
import com.guardtime.envelope.extending.ExtendedEnvelope;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.exception.InvalidEnvelopeException;
import com.guardtime.envelope.util.Util;
import com.guardtime.envelope.verification.VerifiedEnvelope;
import com.guardtime.envelope.verification.result.ResultHolder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EnvelopeCloseableIntegrationTest extends AbstractCommonIntegrationTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        closeAll(envelopeElements);
        cleanUpTempDir();
    }

    private void cleanUpTempDir() throws Exception {
        cleanTempDir();
        assertFalse("Unclean test system! There are some 'ksie' files in " + tmpDir, anyKsieTempFiles());
    }

    @After
    public void assertCleanTempDir() {
        assertFalse("Close did not delete all temporary files!", anyKsieTempFiles());
    }

    @Test
    public void testClosingClosedEnvelopeDoesNotThrowException() throws Exception {
        Envelope envelope = getEnvelope();
        envelope.close();
        envelope.close();
    }

    @Test
    public void testCloseDeletesTemporaryFiles() throws Exception {
        Envelope envelope = getEnvelopeWithTemporaryFileParsingStore(ENVELOPE_WITH_ONE_DOCUMENT);
        assertTrue("Temporary files not found!", anyKsieTempFiles());
        envelope.close();
    }

    @Test
    public void testEnvelopeWithTryWithResources() throws Exception {
        try (Envelope envelope = getEnvelopeWithTemporaryFileParsingStore(ENVELOPE_WITH_ONE_DOCUMENT)) {
            assertFalse(envelope.getSignatureContents().isEmpty());
            assertTrue("Temporary files not found!", anyKsieTempFiles());
        }
    }

    @Test
    public void testDeleteAllTempFilesAndThenCloseEnvelope() throws Exception {
        Envelope envelope = getEnvelope(ENVELOPE_WITH_MULTIPLE_SIGNATURES);
        cleanTempDir();
        envelope.close();
        assertFalse(anyKsieTempFiles());
    }

    @Test
    public void testDeleteAllTempFilesFromExistingEnvelope() throws Exception {
        Envelope existingEnvelope = getEnvelopeWithTemporaryFileParsingStore(ENVELOPE_WITH_MULTIPLE_SIGNATURES);
        List<File> ksieTempFiles = getKsieTempFiles();
        try (
                Document document = new DocumentBuilder()
                        .withDocumentMimeType("byte inputstream")
                        .withDocumentName("byte-input-stream.bis")
                        .withContent(new ByteArrayInputStream(new byte[313]))
                        .build();
                Annotation annotation = new AnnotationBuilder()
                        .withContent("content")
                        .withDomain("domain.com")
                        .withAnnotationType(EnvelopeAnnotationType.FULLY_REMOVABLE)
                    .build();
                Envelope second = packagingFactory.addSignature(
                        existingEnvelope,
                        singletonList(document),
                        singletonList(annotation)
                )
        ) {
            for (File doc : ksieTempFiles) {
                Util.deleteFileOrDirectory(doc.toPath());
            }
            existingEnvelope.close();
            assertFalse(anyKsieTempFiles());
        } finally {
            existingEnvelope.close();
        }
    }

    @Test
    public void testDeleteAllTempFilesFromCreatedEnvelope() throws Exception {
        Envelope existingEnvelope = getEnvelope(ENVELOPE_WITH_MULTIPLE_SIGNATURES);
        List<File> ksieTempFiles = getKsieTempFiles();
        try (
                Document document = new DocumentBuilder()
                        .withDocumentMimeType("byte inputstream")
                        .withDocumentName("byte-input-stream.bis")
                        .withContent(new ByteArrayInputStream(new byte[313]))
                        .build();
                Annotation annotation = new AnnotationBuilder()
                        .withContent("content")
                        .withDomain("domain.com")
                        .withAnnotationType(EnvelopeAnnotationType.FULLY_REMOVABLE)
                        .build();
                Envelope second = packagingFactory.addSignature(
                        existingEnvelope,
                        singletonList(document),
                        singletonList(annotation)
                )
        ) {
            for (File doc : getKsieTempFiles()) {
                if (isTempFile(doc) && !ksieTempFiles.contains(doc)) {
                    Util.deleteFileOrDirectory(doc.toPath());
                }
            }
            assertTrue(ksieTempFiles.equals(getKsieTempFiles()));
        } finally {
            existingEnvelope.close();
        }
        assertFalse(anyKsieTempFiles());
    }

    @Test
    public void testWriteClosedEnvelope() throws Exception {
        expectedException.expect(IOException.class);
        expectedException.expectMessage("Can't write closed object!");
        Envelope envelope = getEnvelope(ENVELOPE_WITH_MULTIPLE_SIGNATURES);
        envelope.close();
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            envelopeWriter.write(envelope, bos);
        }
        assertFalse(anyKsieTempFiles());
    }

    @Test
    public void testWriteEnvelopeWithTempFileRemoved() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Store has been corrupted! Expected to find file");
        try (
                Envelope envelope = getEnvelopeWithTemporaryFileParsingStore(ENVELOPE_WITH_MULTIPLE_SIGNATURES);
                ByteArrayOutputStream bos = new ByteArrayOutputStream()
        ) {
            List<File> tempFiles = getKsieTempFiles();
            // Magic number = 1 doc + 1 annot per signatureContent + 1 unknownFile for ENVELOPE_WITH_MULTIPLE_SIGNATURES
            assertTrue(tempFiles.size() == 5);
            Util.deleteFileOrDirectory(tempFiles.get(0).toPath());
            envelopeWriter.write(envelope, bos);
        }
    }

    @Test
    public void testCreateEnvelopeFromExistingAndAlteredTempFile_ThrowsInvalidPackageException() throws Exception {
        expectedException.expect(InvalidEnvelopeException.class);
        expectedException.expectMessage("Created envelope did not pass internal verification");
        try (
                Document document = new DocumentBuilder()
                        .withDocumentMimeType("qwerty")
                        .withDocumentName("qwert.file")
                        .withContent(new ByteArrayInputStream("randum".getBytes()))
                        .build();
                Annotation annotation = new AnnotationBuilder()
                        .withContent("qwerty file")
                        .withDomain("qwerty.domain.com")
                        .withAnnotationType(EnvelopeAnnotationType.FULLY_REMOVABLE)
                .build();
                Envelope envelope = getEnvelopeWithTemporaryFileParsingStore(ENVELOPE_WITH_NON_REMOVABLE_ANNOTATION)
        ) {
            List<File> tempFiles = getKsieTempFiles();
            //Magic number = 1 document + 1 annotation per signature content for ENVELOPE_WITH_NON_REMOVABLE_ANNOTATION
            assertTrue(
                    "Temp dir contains more than necessary KSIE temporary files, test system is not clean.",
                    tempFiles.size() == 2
            );
            for (File file: tempFiles) {
                try (PrintWriter out = new PrintWriter(file)) {
                    out.write("This is new content for temp file.");
                }
            }
            try (Envelope second = packagingFactory.addSignature(envelope, singletonList(document), singletonList(annotation))) {

            }
        }
    }

    @Test
    public void testCloseSourceEnvelope() throws Exception {
        Envelope envelope = getEnvelope();
        VerifiedEnvelope verifiedEnvelope = new VerifiedEnvelope(envelope, new ResultHolder());
        ExtendedEnvelope extendedEnvelope = new ExtendedEnvelope(envelope);
        envelope.close();
        assertFalse(anyKsieTempFiles());
    }

    @Test
    public void testCloseVerifiedEnvelope() throws Exception {
        Envelope envelope = getEnvelope();
        VerifiedEnvelope verifiedEnvelope = new VerifiedEnvelope(envelope, new ResultHolder());
        verifiedEnvelope.close();
        assertFalse(anyKsieTempFiles());
    }

    @Test
    public void testCloseExtendedEnvelope() throws Exception {
        Envelope envelope = getEnvelope();
        ExtendedEnvelope extendedEnvelope = new ExtendedEnvelope(envelope);
        extendedEnvelope.close();
        assertFalse(anyKsieTempFiles());
    }

}
