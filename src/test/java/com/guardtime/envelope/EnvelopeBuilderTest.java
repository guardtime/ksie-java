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

package com.guardtime.envelope;

import com.guardtime.envelope.document.EnvelopeDocument;
import com.guardtime.envelope.document.StreamEnvelopeDocument;
import com.guardtime.envelope.indexing.IncrementingIndexProviderFactory;
import com.guardtime.envelope.manifest.SignatureReference;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopePackagingFactory;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.packaging.zip.ZipEnvelopePackagingFactoryBuilder;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.signature.SignatureException;
import com.guardtime.envelope.util.Pair;
import com.guardtime.ksi.hashing.DataHash;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class EnvelopeBuilderTest extends AbstractEnvelopeTest {

    @Mock
    private EnvelopePackagingFactory mockedPackagingFactory;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(mockedPackagingFactory.create(Mockito.anyList(), Mockito.anyList())).thenReturn(Mockito.mock(Envelope.class));
    }

    @Test
    public void testCreateBuilder() throws Exception {
        EnvelopeBuilder builder = new EnvelopeBuilder(mockedPackagingFactory);
        assertNotNull(builder);
    }

    @Test
    public void testCreateBuilderWithoutPackagingFactory_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Packaging factory must be present");
        new EnvelopeBuilder(null);
    }

    @Test
    public void testAddDocumentToEnvelope() throws Exception {
        EnvelopeBuilder builder = new EnvelopeBuilder(mockedPackagingFactory);
        try (StreamEnvelopeDocument document = new StreamEnvelopeDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT)) {
            builder.withDocument(document);
            assertEquals(1, builder.getDocuments().size());

            builder.withDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), TEST_FILE_NAME_TEST_DOC, MIME_TYPE_APPLICATION_TXT);
            assertEquals(2, builder.getDocuments().size());

            File mockFile = Mockito.mock(File.class);
            when(mockFile.getName()).thenReturn("SomeName.ext");
            builder.withDocument(mockFile, "application/binary");
            assertEquals(3, builder.getDocuments().size());

            closeAll(builder.getDocuments());
        }
    }

    @Test
    public void testAddAnnotationToEnvelope() throws Exception {
        EnvelopeBuilder builder = new EnvelopeBuilder(mockedPackagingFactory);
        builder.withAnnotation(STRING_ENVELOPE_ANNOTATION);
        assertEquals(1, builder.getAnnotations().size());
    }

    @Test
    public void testCreateSignature() throws Exception {
        EnvelopeBuilder builder = new EnvelopeBuilder(mockedPackagingFactory);
        builder.withDocument(TEST_DOCUMENT_HELLO_TEXT);
        builder.withDocument(TEST_DOCUMENT_HELLO_PDF);

        builder.withAnnotation(STRING_ENVELOPE_ANNOTATION);
        try (Envelope envelope = builder.build()) {
            assertNotNull(envelope);
        }
    }

    @Test
    public void testCreateWithExistingEnvelope() throws Exception {
        EnvelopePackagingFactory packagingFactory = getEnvelopePackagingFactory();

        // build initial envelope
        EnvelopeBuilder builder = new EnvelopeBuilder(packagingFactory);
        builder.withDocument(TEST_DOCUMENT_HELLO_PDF);
        try (Envelope envelope = builder.build()) {

            // add new documents to existing envelope
            builder.withDocument(TEST_DOCUMENT_HELLO_TEXT);
            builder.withExistingEnvelope(envelope);
            try (Envelope newEnvelope = builder.build()) {

                assertNotNull(newEnvelope);
                assertEquals(2, newEnvelope.getSignatureContents().size());
            }
        }
    }

    private EnvelopePackagingFactory getEnvelopePackagingFactory() throws IOException, com.guardtime.envelope.manifest.InvalidManifestException, SignatureException {
        EnvelopePackagingFactory packagingFactory = new ZipEnvelopePackagingFactoryBuilder().
                withSignatureFactory(mockedSignatureFactory).
                withManifestFactory(mockedManifestFactory).
                withIndexProviderFactory(new IncrementingIndexProviderFactory()).
                disableInternalVerification().
                build();

        when(mockedManifestFactory.createManifest(Mockito.any(Pair.class), Mockito.any(Pair.class), Mockito.any(Pair.class))).thenReturn(mockedManifest);
        when(mockedManifest.getManifestFactoryType()).thenReturn(mockedManifestFactoryType);
        when(mockedManifestFactoryType.getManifestFileExtension()).thenReturn("tlv");
        SignatureReference mockedSignatureReference = mock(SignatureReference.class);
        when(mockedManifest.getSignatureReference()).thenReturn(mockedSignatureReference);
        when(mockedSignatureReference.getType()).thenReturn("signatureType");
        when(mockedSignatureReference.getUri()).thenReturn("META-INF/signature-1.ksig");
        EnvelopeSignature mockedSignature = mock(EnvelopeSignature.class);
        when(mockedSignatureFactory.create(any(DataHash.class))).thenReturn(mockedSignature);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                invocationOnMock.getArgumentAt(0, OutputStream.class).write("someData".getBytes());
                return null;
            }
        }) .when(mockedSignature).writeTo(any(OutputStream.class));
        return packagingFactory;
    }

    @Test
    public void testCreateWithMultipleDocumentsWithSameFileName_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expectMessage("Document with name '" + TEST_FILE_NAME_TEST_TXT + "' already exists!");
        expectedException.expect(IllegalArgumentException.class);
        try (
                EnvelopeDocument document = new StreamEnvelopeDocument(new ByteArrayInputStream("ImportantDocument-1".getBytes(StandardCharsets.UTF_8)), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT);
                EnvelopeDocument streamEnvelopeDocument = new StreamEnvelopeDocument(new ByteArrayInputStream("ImportantDocument-2".getBytes(StandardCharsets.UTF_8)), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT)
        ) {
            EnvelopeBuilder builder = new EnvelopeBuilder(mockedPackagingFactory);
            builder.withDocument(document);
            builder.withDocument(streamEnvelopeDocument);
            builder.build();
        }
    }

    @Test
    public void testCreateWithExistingEnvelopeWithMultipleDocumentsWithSameFileName_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Found multiple documents with same name!");
        try (
                EnvelopeDocument document = new StreamEnvelopeDocument(new ByteArrayInputStream("ImportantDocument-2".getBytes(StandardCharsets.UTF_8)), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT);
                EnvelopeDocument streamEnvelopeDocument = new StreamEnvelopeDocument(new ByteArrayInputStream("ImportantDocument-HAHA".getBytes(StandardCharsets.UTF_8)), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT)
        ) {
            EnvelopePackagingFactory packagingFactory = getEnvelopePackagingFactory();
            // build initial envelope
            EnvelopeBuilder builder = new EnvelopeBuilder(packagingFactory);
            builder.withDocument(document);
            try (Envelope envelope = builder.build()) {

                // add new documents to existing envelope
                builder.withDocument(streamEnvelopeDocument);
                builder.withExistingEnvelope(envelope);
                builder.build();
            }
        }
    }

    @Test
    public void testCreateNewEnvelopeUsingExistingEnvelopeAndExistingDocument() throws Exception {
        EnvelopePackagingFactory packagingFactory = getEnvelopePackagingFactory();
        // build initial envelope
        EnvelopeBuilder builder = new EnvelopeBuilder(packagingFactory);
        builder.withDocument(TEST_DOCUMENT_HELLO_PDF);

        try (Envelope envelope = builder.build()) {
            // add new documents to existing envelope
            builder.withExistingEnvelope(envelope);
            builder.withDocument(TEST_DOCUMENT_HELLO_PDF);

            try (Envelope newEnvelope = builder.build()) {
                assertNotNull(newEnvelope);
                assertEquals(2, newEnvelope.getSignatureContents().size());
                Set<String> documentPaths = new HashSet<>();
                for (SignatureContent content : newEnvelope.getSignatureContents()) {
                    documentPaths.addAll(content.getDocuments().keySet());
                }
                assertEquals(1, documentPaths.size());
            }
        }
    }

}
