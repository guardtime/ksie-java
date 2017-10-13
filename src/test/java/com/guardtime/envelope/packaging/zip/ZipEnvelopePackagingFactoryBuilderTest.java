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
import com.guardtime.envelope.annotation.EnvelopeAnnotationType;
import com.guardtime.envelope.annotation.StringEnvelopeAnnotation;
import com.guardtime.envelope.document.EnvelopeDocument;
import com.guardtime.envelope.document.StreamEnvelopeDocument;
import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.manifest.tlv.TlvEnvelopeManifestFactory;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopePackagingFactory;
import com.guardtime.envelope.packaging.MimeTypeEntry;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.packaging.exception.InvalidPackageException;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.util.Pair;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ZipEnvelopePackagingFactoryBuilderTest extends AbstractEnvelopeTest {
    private static final DataHash nullDataHash = new DataHash(HashAlgorithm.SHA2_256, new byte[32]);
    private List<EnvelopeAnnotation> envelopeAnnotationList = new ArrayList<>();
    private List<EnvelopeDocument> envelopeDocumentList = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        super.setUp();
        envelopeAnnotationList.add(new StringEnvelopeAnnotation(EnvelopeAnnotationType.NON_REMOVABLE, ANNOTATION_CONTENT, ANNOTATION_DOMAIN_COM_GUARDTIME));
        envelopeDocumentList.add(TEST_DOCUMENT_HELLO_TEXT);
    }

    @After
    public void cleanUp() throws Exception {
        closeAll(envelopeAnnotationList);
        closeAll(envelopeDocumentList);
    }

    private Envelope createInternallyValidEnvelope(List<EnvelopeDocument> documents, List<EnvelopeAnnotation> annotations) throws Exception {
        return createInternallyValidEnvelope(documents, annotations, null);
    }

    private Envelope createInternallyValidEnvelope(List<EnvelopeDocument> documents, List<EnvelopeAnnotation> annotations, Envelope existingEnvelope) throws Exception {
        TlvEnvelopeManifestFactory manifestFactorySpy = spy(new TlvEnvelopeManifestFactory());
        EnvelopePackagingFactory packagingFactory = new ZipEnvelopePackagingFactoryBuilder().
                withSignatureFactory(mockedSignatureFactory).
                withManifestFactory(manifestFactorySpy).
                build();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Manifest spyManifest = spy((Manifest) invocationOnMock.callRealMethod());
                doReturn(nullDataHash).when(spyManifest).getDataHash(any(HashAlgorithm.class));
                return spyManifest;
            }
        }).when(manifestFactorySpy).createManifest(any(Pair.class), any(Pair.class), any(Pair.class));
        EnvelopeSignature mockSignature = mock(EnvelopeSignature.class);
        when(mockSignature.getSignature()).thenReturn("I decree this to be authentic!");
        when(mockSignature.getSignedDataHash()).thenReturn(nullDataHash);
        when(mockedSignatureFactory.create(any(DataHash.class))).thenReturn(mockSignature);
        if (existingEnvelope != null) {
            packagingFactory.addSignature(existingEnvelope, documents, annotations);
            return existingEnvelope;
        }
        return packagingFactory.create(documents, annotations);
    }

    @Test
    public void testCreatePackagingFactoryWithoutSignatureFactory_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Signature factory must be present");
        new ZipEnvelopePackagingFactoryBuilder().withSignatureFactory(null).build();
    }

    @Test
    public void testCreatePackagingFactoryWithoutParsingStoreFactory_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Parsing store factory must be present");
        new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(mockedSignatureFactory)
                .withManifestFactory(mockedManifestFactory)
                .withParsingStoreFactory(null)
                .build();
    }

    @Test
    public void testCreatePackagingFactoryWithoutManifestFactory_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Manifest factory must be present");
        new ZipEnvelopePackagingFactoryBuilder().
                withSignatureFactory(mockedSignatureFactory).
                withManifestFactory(null).
                build();
    }

    @Test
    public void testCreatePackagingFactoryWithoutIndexProviderFactory_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Index provider factory must be present");
        new ZipEnvelopePackagingFactoryBuilder().
                withSignatureFactory(mockedSignatureFactory).
                withIndexProviderFactory(null).
                build();
    }

    @Test
    public void testCreatePackagingFactoryWithoutDocuments_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Document files must not be empty");
        createInternallyValidEnvelope(new ArrayList<EnvelopeDocument>(), envelopeAnnotationList);
    }

    @Test
    public void testCreateEnvelopeWithDocument() throws Exception {
        try (Envelope envelope = createInternallyValidEnvelope(envelopeDocumentList, null)) {
            assertNotNull(envelope);
        }
    }

    @Test
    public void testCreateEnvelopeWithDocumentAndAnnotation() throws Exception {
        try (Envelope envelope = createInternallyValidEnvelope(envelopeDocumentList, envelopeAnnotationList)) {
            assertNotNull(envelope);
        }
    }

    @Test
    public void testCreateEnvelopeWithMultipleDocuments() throws Exception {
        List<EnvelopeDocument> documentsList = new ArrayList<>(envelopeDocumentList);
        documentsList.add(TEST_DOCUMENT_HELLO_PDF);

        try (Envelope envelope = createInternallyValidEnvelope(documentsList, null)) {
            assertNotNull(envelope);
            Collection<EnvelopeDocument> containedDocuments = envelope.getSignatureContents().get(0).getDocuments().values();
            assertNotNull(containedDocuments);
            assertTrue(containedDocuments.containsAll(documentsList));
        }
    }

    @Test
    public void testCreateEnvelopeWithMultipleDocumentsAndAnnotations() throws Exception {
        List<EnvelopeDocument> documentsList = new ArrayList<>(envelopeDocumentList);
        documentsList.add(TEST_DOCUMENT_HELLO_PDF);
        List<EnvelopeAnnotation> annotationsList = new ArrayList<>(envelopeAnnotationList);
        annotationsList.add(new StringEnvelopeAnnotation(EnvelopeAnnotationType.VALUE_REMOVABLE, "moreContent", "com.guardtime.test.inner"));

        try (Envelope envelope = createInternallyValidEnvelope(documentsList, annotationsList)) {
            assertNotNull(envelope);
            Collection<EnvelopeAnnotation> containedAnnotations = envelope.getSignatureContents().get(0).getAnnotations().values();
            assertNotNull(containedAnnotations);
            assertTrue(containedAnnotations.containsAll(annotationsList));
        }
    }

    @Test
    public void testCreateEnvelopeWithExistingEnvelopeAndMultipleDocumentsAndAnnotations() throws Exception {
        try (
                Envelope envelope = createInternallyValidEnvelope(envelopeDocumentList, envelopeAnnotationList);
                EnvelopeDocument streamEnvelopeDocument = new StreamEnvelopeDocument(new ByteArrayInputStream("ImportantDocument-1".getBytes(StandardCharsets.UTF_8)), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_DOC)
        ) {
            List<EnvelopeDocument> documentsList = new ArrayList<>();
            documentsList.add(TEST_DOCUMENT_HELLO_PDF);
            documentsList.add(streamEnvelopeDocument);
            List<EnvelopeAnnotation> annotationsList = new ArrayList<>(envelopeAnnotationList);
            annotationsList.add(new StringEnvelopeAnnotation(EnvelopeAnnotationType.VALUE_REMOVABLE, "moreContent", "com.guardtime.test.inner"));

            try (Envelope newEnvelope = createInternallyValidEnvelope(documentsList, annotationsList, envelope)) {
                assertNotNull(newEnvelope);
                Collection<EnvelopeDocument> containedDocuments = newEnvelope.getSignatureContents().get(1).getDocuments().values();
                assertNotNull(containedDocuments);
                assertTrue(containedDocuments.containsAll(documentsList));
                Collection<EnvelopeAnnotation> containedAnnotations = newEnvelope.getSignatureContents().get(1).getAnnotations().values();
                assertNotNull(containedAnnotations);
                assertTrue(containedAnnotations.containsAll(annotationsList));
            }
        }
    }

    @Test
    public void testCreateEnvelopeWithMultipleDocumentsWithSameName_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Found multiple documents with same name!");

        List<EnvelopeDocument> envelopeDocuments = Arrays.asList(
                (EnvelopeDocument) new StreamEnvelopeDocument(new ByteArrayInputStream("ImportantDocument-1".getBytes(StandardCharsets.UTF_8)), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT),
                new StreamEnvelopeDocument(new ByteArrayInputStream("ImportantDocument-2".getBytes(StandardCharsets.UTF_8)), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT)
        );
        createInternallyValidEnvelope(envelopeDocuments, null);
    }

    @Test
    public void testCreateVerifiesEnvelope_OK() throws Exception {
        try (Envelope envelope = createInternallyValidEnvelope(envelopeDocumentList, envelopeAnnotationList)) {
            assertNotNull(envelope);
        }
    }

    @Test
    public void testCreateVerifiesInvalidEnvelope_NOK() throws Exception {
        expectedException.expect(InvalidPackageException.class);
        expectedException.expectMessage("Created envelope did not pass internal verification");
        EnvelopePackagingFactory packagingFactory = new ZipEnvelopePackagingFactoryBuilder().withSignatureFactory(mockedSignatureFactory).build();
        packagingFactory.create(envelopeDocumentList, envelopeAnnotationList);
    }

    @Test
    public void testCreateWithExistingEnvelopeVerifiesEnvelope_OK() throws Exception {
        try (
                EnvelopeDocument streamEnvelopeDocument = new StreamEnvelopeDocument(new ByteArrayInputStream("ImportantDocument-1".getBytes(StandardCharsets.UTF_8)), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_PDF);
                Envelope envelope = createInternallyValidEnvelope(envelopeDocumentList, envelopeAnnotationList);
                Envelope newEnvelope = createInternallyValidEnvelope(Collections.singletonList(streamEnvelopeDocument), envelopeAnnotationList, envelope)
        ) {
            assertNotNull(newEnvelope);
        }
    }

    @Test
    public void testCreateWithExistingEnvelopeVerifiesInvalidEnvelope_NOK() throws Exception {
        expectedException.expect(InvalidPackageException.class);
        expectedException.expectMessage("Created envelope did not pass internal verification");
        EnvelopePackagingFactory packagingFactory = new ZipEnvelopePackagingFactoryBuilder().withSignatureFactory(mockedSignatureFactory).build();
        Envelope mockEnvelope = Mockito.mock(Envelope.class);
        when(mockEnvelope.getMimeType()).thenReturn(new MimeTypeEntry("MIMETYPE", "Ploomimoos".getBytes(StandardCharsets.UTF_8)));
        packagingFactory.addSignature(mockEnvelope, envelopeDocumentList, envelopeAnnotationList);
    }

    @Test
    public void testCreateEnvelopeWithExistingEnvelopeWithDocumentsWithSameName_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Found multiple documents with same name!");
        try (
                EnvelopeDocument envelopeDocument = new StreamEnvelopeDocument(new ByteArrayInputStream("ImportantDocument-1".getBytes(StandardCharsets.UTF_8)), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT);
                EnvelopeDocument streamEnvelopeDocument = new StreamEnvelopeDocument(new ByteArrayInputStream("MoreImportantDocument-0411".getBytes(StandardCharsets.UTF_8)), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT)
        ) {
            List<EnvelopeDocument> envelopeDocuments = Collections.singletonList(envelopeDocument);
            Envelope existingEnvelope = createInternallyValidEnvelope(envelopeDocuments, null);

            List<EnvelopeDocument> newEnvelopeDocuments = Collections.singletonList(streamEnvelopeDocument);
            createInternallyValidEnvelope(newEnvelopeDocuments, null, existingEnvelope);
        }
    }

    @Test
    public void testCreateEnvelopeWithExistingEnvelopeWithSameDocument() throws Exception {
        try (
                EnvelopeDocument streamEnvelopeDocument = new StreamEnvelopeDocument(new ByteArrayInputStream("ImportantDocument-1".getBytes(StandardCharsets.UTF_8)), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT);
                Envelope envelope = createInternallyValidEnvelope(Collections.singletonList(streamEnvelopeDocument), null);
                Envelope newEnvelope = createInternallyValidEnvelope(Collections.singletonList(streamEnvelopeDocument), null, envelope)
        ) {
            Set<String> documentPaths = new HashSet<>();
            for (SignatureContent content : newEnvelope.getSignatureContents()) {
                documentPaths.addAll(content.getDocuments().keySet());
            }
            assertEquals(1, documentPaths.size());
        }
    }

    @Test
    public void testReadFromBadStream_ThrowsInvalidPackageException() throws Exception {
        expectedException.expect(InvalidPackageException.class);
        expectedException.expectMessage("Failed to parse InputStream");
        EnvelopePackagingFactory packagingFactory = new ZipEnvelopePackagingFactoryBuilder().
                withSignatureFactory(mockedSignatureFactory).
                withManifestFactory(new TlvEnvelopeManifestFactory()).
                build();
        InputStream inputStream = spy(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));
        doThrow(IOException.class).when(inputStream).close();
        packagingFactory.read(inputStream);
    }

}