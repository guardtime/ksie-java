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
import com.guardtime.envelope.annotation.EnvelopeAnnotationType;
import com.guardtime.envelope.document.Document;
import com.guardtime.envelope.manifest.AnnotationsManifest;
import com.guardtime.envelope.manifest.DocumentsManifest;
import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.manifest.tlv.TlvEnvelopeManifestFactory;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopePackagingFactory;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.packaging.exception.InvalidEnvelopeException;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.signature.SignatureFactoryType;
import com.guardtime.envelope.verification.policy.LimitedInternalVerificationPolicy;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.unisignature.KSISignature;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ZipEnvelopePackagingFactoryBuilderTest extends AbstractEnvelopeTest {
    private static final DataHash NULL_DATA_HASH = new DataHash(HashAlgorithm.SHA2_256, new byte[32]);
    private List<Annotation> annotationList = new ArrayList<>();
    private List<Document> documentList = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        super.setUp();
        annotationList.add(
                annotationFactory.create(
                        ANNOTATION_CONTENT,
                        ANNOTATION_DOMAIN_COM_GUARDTIME,
                        EnvelopeAnnotationType.NON_REMOVABLE
                )
        );
        documentList.add(testDocumentHelloText);
    }

    @After
    public void cleanUp() throws Exception {
        closeAll(annotationList);
        closeAll(documentList);
    }

    private Envelope createInternallyValidEnvelope(List<Document> documents, List<Annotation> annotations) throws Exception {
        return createInternallyValidEnvelope(documents, annotations, null);
    }

    private Envelope createInternallyValidEnvelope(List<Document> documents, List<Annotation> annotations,
                                                   Envelope existingEnvelope) throws Exception {
        TlvEnvelopeManifestFactory manifestFactorySpy = spy(new TlvEnvelopeManifestFactory());
        EnvelopePackagingFactory packagingFactory = new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(mockedSignatureFactory)
                .withManifestFactory(manifestFactorySpy)
                .withParsingStore(parsingStore)
                .build();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Manifest spyManifest = spy((Manifest) invocationOnMock.callRealMethod());
                doReturn(NULL_DATA_HASH).when(spyManifest).getDataHash(any(HashAlgorithm.class));
                return spyManifest;
            }
        }).when(manifestFactorySpy).createManifest(
                any(DocumentsManifest.class),
                any(AnnotationsManifest.class),
                any(SignatureFactoryType.class),
                anyString(),
                anyString()
        );
        EnvelopeSignature mockSignature = mock(EnvelopeSignature.class);
        when(mockSignature.compareTo(any(EnvelopeSignature.class))).thenReturn(-1);
        KSISignature mockKSISignature = mock(KSISignature.class);
        when(mockKSISignature.getAggregationTime()).thenReturn(new Date());
        when(mockSignature.getSignature()).thenReturn(mockKSISignature);
        when(mockSignature.getSignedDataHash()).thenReturn(NULL_DATA_HASH);
        when(mockSignature.getCopy()).thenReturn(mockSignature);
        when(mockedSignatureFactory.create(any(DataHash.class))).thenReturn(mockSignature);
        if (existingEnvelope != null) {
            return packagingFactory.addSignature(existingEnvelope, documents, annotations);
        }
        return packagingFactory.create(documents, annotations);
    }

    @Test
    public void testCreateZipPackagingFactoryWithoutSignatureFactory_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Signature factory must be present");
        new ZipEnvelopePackagingFactoryBuilder().withSignatureFactory(null).build();
    }

    @Test
    public void testCreateZipPackagingFactoryWithoutParsingStore_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Parsing store must be present");
        new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(mockedSignatureFactory)
                .withParsingStore(null)
                .build();
    }

    @Test
    public void testCreateZipPackagingFactoryWithoutManifestFactory_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Manifest factory must be present");
        new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(mockedSignatureFactory)
                .withParsingStore(parsingStore)
                .withManifestFactory(null)
                .build();
    }

    @Test
    public void testCreateZipPackagingFactoryWithoutIndexProviderFactory_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Index provider factory must be present");
        new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(mockedSignatureFactory)
                .withParsingStore(parsingStore)
                .withIndexProviderFactory(null)
                .build();
    }

    @Test
    public void testCreateZipPackagingFactoryWithoutEnvelopeReader_Ok() throws Exception {
        new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(mockedSignatureFactory)
                .withParsingStore(parsingStore)
                .withEnvelopeReader(null)
                .build();
    }

    @Test
    public void testCreatePackagingFactoryWithNoVerificationPolicy_Ok() throws Exception {
        new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(mockedSignatureFactory)
                .withParsingStore(parsingStore)
                .withVerificationPolicy(null)
                .build();
    }

    @Test
    public void testCreatePackagingFactoryWithNotDefaultVerificationPolicy_Ok() throws Exception {
        new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(mockedSignatureFactory)
                .withParsingStore(parsingStore)
                .withVerificationPolicy(new LimitedInternalVerificationPolicy())
                .build();
    }

    @Test
    public void testCreatePackagingFactoryWithoutDocuments_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Document files must not be empty");
        createInternallyValidEnvelope(new ArrayList<Document>(), annotationList);
    }

    @Test
    public void testCreateEnvelopeWithDocument() throws Exception {
        try (Envelope envelope = createInternallyValidEnvelope(documentList, null)) {
            assertNotNull(envelope);
        }
    }

    @Test
    public void testCreateEnvelopeWithDocumentAndAnnotation() throws Exception {
        try (Envelope envelope = createInternallyValidEnvelope(documentList, annotationList)) {
            assertNotNull(envelope);
        }
    }

    @Test
    public void testCreateEnvelopeWithMultipleDocuments() throws Exception {
        List<Document> documentsList = new ArrayList<>(documentList);
        documentsList.add(testDocumentHelloPdf);

        try (Envelope envelope = createInternallyValidEnvelope(documentsList, null)) {
            assertNotNull(envelope);
            Collection<Document> containedDocuments = envelope.getSignatureContents().get(0).getDocuments().values();
            assertNotNull(containedDocuments);
            assertTrue(containedDocuments.containsAll(documentsList));
        }
    }

    @Test
    public void testCreateEnvelopeWithMultipleDocumentsAndAnnotations() throws Exception {
        List<Document> documentsList = new ArrayList<>(documentList);
        documentsList.add(testDocumentHelloPdf);
        List<Annotation> annotationsList = new ArrayList<>(annotationList);
        annotationsList.add(
                annotationFactory.create("moreContent", "com.guardtime.test.inner", EnvelopeAnnotationType.VALUE_REMOVABLE)
        );

        try (Envelope envelope = createInternallyValidEnvelope(documentsList, annotationsList)) {
            assertNotNull(envelope);
            Collection<Annotation> containedAnnotations = envelope.getSignatureContents().get(0).getAnnotations().values();
            assertNotNull(containedAnnotations);
            assertTrue(containedAnnotations.containsAll(annotationsList));
        }
    }

    @Test
    public void testCreateEnvelopeWithExistingEnvelopeAndMultipleDocumentsAndAnnotations() throws Exception {
        try (
                Envelope envelope = createInternallyValidEnvelope(documentList, annotationList);
                Document streamDocument =
                        documentFactory.create(
                                new ByteArrayInputStream("ImportantDocument-1".getBytes(StandardCharsets.UTF_8)),
                                MIME_TYPE_APPLICATION_TXT,
                                TEST_FILE_NAME_TEST_DOC
                        )
        ) {
            List<Document> documentsList = new ArrayList<>();
            documentsList.add(testDocumentHelloPdf);
            documentsList.add(streamDocument);
            List<Annotation> annotationsList = new ArrayList<>(annotationList);
            annotationsList.add(
                    annotationFactory.create("moreContent", "com.guardtime.test.inner", EnvelopeAnnotationType.VALUE_REMOVABLE)
            );

            try (Envelope newEnvelope = createInternallyValidEnvelope(documentsList, annotationsList, envelope)) {
                assertNotNull(newEnvelope);
                Collection<Document> containedDocuments = newEnvelope.getSignatureContents().get(0).getDocuments().values();
                assertNotNull(containedDocuments);
                assertTrue(customContains(containedDocuments, documentsList));
                Collection<Annotation> containedAnnotations =
                        newEnvelope.getSignatureContents().get(0).getAnnotations().values();
                assertNotNull(containedAnnotations);
                assertTrue(customContains(containedAnnotations, annotationsList));
            }
        }
    }

    private boolean customContains(Collection<?> container, Collection<?> contained) {
        for (Object o : contained) {
            if (!container.contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Test
    public void testCreateEnvelopeWithMultipleDocumentsWithSameName_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Found multiple documents with same name and non-matching data hash!");

        List<Document> documents = Arrays.asList(
                documentFactory.create(
                        new ByteArrayInputStream("ImportantDocument-1".getBytes(StandardCharsets.UTF_8)),
                        MIME_TYPE_APPLICATION_TXT,
                        TEST_FILE_NAME_TEST_TXT
                ),
                documentFactory.create(
                        new ByteArrayInputStream("ImportantDocument-2".getBytes(StandardCharsets.UTF_8)),
                        MIME_TYPE_APPLICATION_TXT,
                        TEST_FILE_NAME_TEST_TXT
                )
        );
        createInternallyValidEnvelope(documents, null);
    }

    @Test
    public void testCreateVerifiesEnvelope_OK() throws Exception {
        try (Envelope envelope = createInternallyValidEnvelope(documentList, annotationList)) {
            assertNotNull(envelope);
        }
    }

    @Test
    public void testCreateVerifiesInvalidEnvelope_NOK() throws Exception {
        expectedException.expect(InvalidEnvelopeException.class);
        expectedException.expectMessage("Created envelope did not pass internal verification");
        EnvelopePackagingFactory packagingFactory =
                new ZipEnvelopePackagingFactoryBuilder()
                        .withSignatureFactory(mockedSignatureFactory)
                        .withParsingStore(parsingStore)
                        .build();
        packagingFactory.create(documentList, annotationList);
    }

    @Test
    public void testCreateWithExistingEnvelopeVerifiesEnvelope_OK() throws Exception {
        try (
                Document streamDocument =
                        documentFactory.create(
                                new ByteArrayInputStream("ImportantDocument-1".getBytes(StandardCharsets.UTF_8)),
                                MIME_TYPE_APPLICATION_TXT,
                                TEST_FILE_NAME_TEST_PDF
                        );
                Envelope envelope = createInternallyValidEnvelope(documentList, annotationList);
                Envelope newEnvelope = createInternallyValidEnvelope(singletonList(streamDocument), annotationList, envelope)
        ) {
            assertNotNull(newEnvelope);
        }
    }

    @Test
    public void testCreateEnvelopeWithExistingEnvelopeWithDocumentsWithSameName_ThrowsIllegalArgumentException()
            throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Found multiple documents with same name and non-matching data hash!");
        try (
                Document document =
                        documentFactory.create(
                                new ByteArrayInputStream("ImportantDocument-1".getBytes(StandardCharsets.UTF_8)),
                                MIME_TYPE_APPLICATION_TXT,
                                TEST_FILE_NAME_TEST_TXT
                        );
                Document streamDocument = documentFactory.create(
                        new ByteArrayInputStream("MoreImportantDocument-0411".getBytes(StandardCharsets.UTF_8)),
                        MIME_TYPE_APPLICATION_TXT,
                        TEST_FILE_NAME_TEST_TXT
                )
        ) {
            List<Document> documents = singletonList(document);
            Envelope existingEnvelope = createInternallyValidEnvelope(documents, null);

            List<Document> newDocuments = singletonList(streamDocument);
            try (Envelope newEnvelope = createInternallyValidEnvelope(newDocuments, null, existingEnvelope)) {

            }
        }
    }

    @Test
    public void testCreateEnvelopeWithExistingEnvelopeWithSameDocument() throws Exception {
        try (
                Document streamDocument =
                        documentFactory.create(
                                new ByteArrayInputStream("ImportantDocument-1".getBytes(StandardCharsets.UTF_8)),
                                MIME_TYPE_APPLICATION_TXT,
                                TEST_FILE_NAME_TEST_TXT
                        );
                Envelope envelope = createInternallyValidEnvelope(singletonList(streamDocument), null);
                Envelope newEnvelope = createInternallyValidEnvelope(singletonList(streamDocument), null, envelope)
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
        expectedException.expect(InvalidEnvelopeException.class);
        expectedException.expectMessage("Failed to parse InputStream");
        EnvelopePackagingFactory packagingFactory = new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(mockedSignatureFactory)
                .withManifestFactory(new TlvEnvelopeManifestFactory())
                .withParsingStore(parsingStore)
                .build();
        InputStream inputStream = spy(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));
        doThrow(IOException.class).when(inputStream).close();
        packagingFactory.read(inputStream);
    }

}
