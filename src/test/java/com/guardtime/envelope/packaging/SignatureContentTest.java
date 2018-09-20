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

package com.guardtime.envelope.packaging;

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.document.Document;
import com.guardtime.envelope.document.EmptyDocument;
import com.guardtime.envelope.manifest.FileReference;
import com.guardtime.envelope.manifest.SingleAnnotationManifest;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.util.Util;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class SignatureContentTest extends AbstractEnvelopeTest {

    @Test
    public void testAttachDocumentData_OK() throws Exception {
        SignatureContent testable = getTestableContent();
        byte[] bytes = "testData".getBytes();
        assertTrue(testable.attachDetachedDocument(TEST_FILE_NAME_TEST_DOC, new ByteArrayInputStream(bytes), documentFactory));
        Document reAttachedDocument = testable.getDocuments().get(TEST_FILE_NAME_TEST_DOC);
        assertFalse(reAttachedDocument instanceof EmptyDocument);
        try (InputStream inputStream = reAttachedDocument.getInputStream()) {
            assertNotNull(inputStream);
            byte[] documentBytes = Util.toByteArray(inputStream);
            assertTrue(Arrays.equals(bytes, documentBytes));
        }
    }

    @Test
    public void testAttachDocumentDataToUnknownDocument_ReturnsFalse() {
        SignatureContent testable = getTestableContent();
        assertFalse(
                testable.attachDetachedDocument(
                        "SomePathThatShouldNotExist",
                        new ByteArrayInputStream(new byte[0]),
                        documentFactory
                )
        );
    }

    @Test
    public void testDetachDocumentData_OK() throws ParsingStoreException {
        SignatureContent testable = getTestableContent();
        int documentsCount = testable.getDocuments().size();
        Document detached = testable.detachDocument(TEST_FILE_NAME_TEST_PDF, documentFactory);
        assertEquals(documentsCount, testable.getDocuments().size());
        assertNotNull(detached);
        assertFalse(detached instanceof EmptyDocument);
        assertNotNull(testable.getDocuments().get(TEST_FILE_NAME_TEST_PDF));
        assertTrue(testable.getDocuments().get(TEST_FILE_NAME_TEST_PDF) instanceof EmptyDocument);
    }

    @Test
    public void testDetachDocumentDataForNonExistentDocument_ReturnsNull() throws ParsingStoreException {
        SignatureContent testable = getTestableContent();
        Document detached = testable.detachDocument("ThisFileIsNotInTheSignatureContent", documentFactory);
        assertNull(detached);
    }

    private SignatureContent getTestableContent() {
        List<Document> documents = Arrays.asList(
                testDocumentHelloText,
                testDocumentHelloPdf,
                documentFactory.create(
                        singletonList(new DataHash(HashAlgorithm.SHA2_256, new byte[32])),
                        "application/doc",
                        TEST_FILE_NAME_TEST_DOC
                )
        );
        when(mockedDocumentsManifest.getDocumentReferences()).thenAnswer(makeFileReferenceList(documents));
        when(mockedDocumentsManifest.getPath()).thenReturn("datamanifest.tlv");
        return new SignatureContent.Builder()
                .withDocuments(documents)
                .withAnnotations(Collections.<Annotation>emptyList())
                .withSingleAnnotationManifests(Collections.<SingleAnnotationManifest>emptyList())
                .withDocumentsManifest(mockedDocumentsManifest)
                .build();
    }

    private Answer<List<? extends FileReference>> makeFileReferenceList(List<Document> documents) {

        final List<FileReference> fileReferenceList = new ArrayList<>();
        for (final Document doc : documents) {
            fileReferenceList.add(new FileReference() {
                @Override
                public String getUri() {
                    return doc.getFileName();
                }

                @Override
                public String getMimeType() {
                    return doc.getMimeType();
                }

                @Override
                public List<DataHash> getHashList() {
                    return singletonList(new DataHash(HashAlgorithm.SHA2_256, new byte[32]));
                }
            });
        }
        return new Answer<List<? extends FileReference>>() {
            @Override
            public List<? extends FileReference> answer(InvocationOnMock invocationOnMock) {
                return fileReferenceList;
            }
        };
    }

}
