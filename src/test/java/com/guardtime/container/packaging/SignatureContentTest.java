package com.guardtime.container.packaging;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.EmptyContainerDocument;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.parsing.store.ParsingStoreException;
import com.guardtime.container.util.Pair;
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

public class SignatureContentTest extends AbstractContainerTest {

    @Test
    public void testAttachDocumentData_OK() throws Exception {
        SignatureContent testable = getTestableContent();
        byte[] bytes = "testData".getBytes();
        assertTrue(testable.attachDetachedDocument(TEST_FILE_NAME_TEST_DOC, new ByteArrayInputStream(bytes)));
        ContainerDocument reAttachedDocument = testable.getDocuments().get(TEST_FILE_NAME_TEST_DOC);
        assertFalse(reAttachedDocument instanceof EmptyContainerDocument);
        try (InputStream inputStream = reAttachedDocument.getInputStream()) {
            assertNotNull(inputStream);
            byte[] documentBytes = Util.toByteArray(inputStream);
            assertTrue(Arrays.equals(bytes, documentBytes));
        }
    }

    @Test
    public void testAttachDocumentDataToUnknownDocument_ReturnsFalse() {
        SignatureContent testable = getTestableContent();
        assertFalse(testable.attachDetachedDocument("SomePathThatShouldNotExist", new ByteArrayInputStream(new byte[0])));
    }

    @Test
    public void testDetachDocumentData_OK() throws ParsingStoreException {
        SignatureContent testable = getTestableContent();
        int documentsCount = testable.getDocuments().size();
        ContainerDocument detached = testable.detachDocument(TEST_FILE_NAME_TEST_PDF);
        assertEquals(documentsCount, testable.getDocuments().size());
        assertNotNull(detached);
        assertFalse(detached instanceof EmptyContainerDocument);
        assertNotNull(testable.getDocuments().get(TEST_FILE_NAME_TEST_PDF));
        assertTrue(testable.getDocuments().get(TEST_FILE_NAME_TEST_PDF) instanceof EmptyContainerDocument);
    }

    @Test
    public void testDetachDocumentDataForNonExistentDocument_ReturnsNull() throws ParsingStoreException {
        SignatureContent testable = getTestableContent();
        ContainerDocument detached = testable.detachDocument("ThisFileIsNotInTheSignatureContent");
        assertNull(detached);
    }

    private SignatureContent getTestableContent() {
        List<ContainerDocument> documents = Arrays.asList(
                TEST_DOCUMENT_HELLO_TEXT,
                TEST_DOCUMENT_HELLO_PDF,
                new EmptyContainerDocument(TEST_FILE_NAME_TEST_DOC, "application/doc", singletonList(new DataHash(HashAlgorithm.SHA2_256, new byte[32])))
        );
        when(mockedDocumentsManifest.getDocumentReferences()).thenAnswer(makeFileReferenceList(documents));
        return new SignatureContent.Builder()
                .withDocuments(documents)
                .withAnnotations(Collections.<Pair<String, ContainerAnnotation>>emptyList())
                .withSingleAnnotationManifests(Collections.<Pair<String, SingleAnnotationManifest>>emptyList())
                .withDocumentsManifest(Pair.of("datamanifest.tlv", mockedDocumentsManifest))
                .build();
    }

    private Answer<List<? extends FileReference>> makeFileReferenceList(List<ContainerDocument> documents) {

        final List<FileReference> fileReferenceList = new ArrayList<>();
        for (final ContainerDocument doc : documents) {
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
            public List<? extends FileReference> answer(InvocationOnMock invocationOnMock) throws Throwable {
                return fileReferenceList;
            }
        };
    }

}