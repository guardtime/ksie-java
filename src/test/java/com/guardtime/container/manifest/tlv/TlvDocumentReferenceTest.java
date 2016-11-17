package com.guardtime.container.manifest.tlv;

import com.guardtime.container.hash.HashAlgorithmProvider;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class TlvDocumentReferenceTest extends AbstractTlvManifestTest {

    @Test
    public void testCreateDocumentReference() throws Exception {
        TlvDocumentReference reference = new TlvDocumentReference(TEST_DOCUMENT_HELLO_TEXT, DEFAULT_HASH_ALGORITHM_PROVIDER);
        assertEquals(DOCUMENT_REFERENCE_TYPE, reference.getElementType());
        assertEquals(MIME_TYPE_APPLICATION_TXT, getMimeType(reference));
        assertEquals(TEST_FILE_NAME_TEST_TXT, getUri(reference));
        assertEquals(dataHash, getDataHash(reference));
    }

    @Test
    public void testReadDocumentReference() throws Exception {
        TLVElement element = createReference(DOCUMENT_REFERENCE_TYPE, TEST_FILE_NAME_TEST_TXT, MIME_TYPE_APPLICATION_TXT, dataHash);
        TlvDocumentReference reference = new TlvDocumentReference(element);
        assertEquals(TEST_FILE_NAME_TEST_TXT, reference.getUri());
        assertEquals(MIME_TYPE_APPLICATION_TXT, reference.getMimeType());
        assertEquals(dataHash, reference.getHashList().get(0));
    }

    @Test
    public void testReadDocumentReferenceWithMultipleHashes() throws Exception {
        List<DataHash> dataHashList = new LinkedList<>();
        dataHashList.add(dataHash);
        dataHashList.add(new DataHash(HashAlgorithm.SHA2_384, "123456789012345678901234567890123456789012345678".getBytes()));
        dataHashList.add(new DataHash(HashAlgorithm.SHA2_512, "1234567890123456789012345678909812345678901234567890123456789098".getBytes()));
        TLVElement element = createReference(DOCUMENT_REFERENCE_TYPE, TEST_FILE_NAME_TEST_TXT, MIME_TYPE_APPLICATION_TXT, dataHashList);
        TlvDocumentReference reference = new TlvDocumentReference(element);
        assertEquals(TEST_FILE_NAME_TEST_TXT, reference.getUri());
        assertEquals(MIME_TYPE_APPLICATION_TXT, reference.getMimeType());
        assertEquals(dataHash, reference.getHashList().get(0));
    }

    @Test
    public void testCreateDocumentReferenceWithMultipleHashes() throws Exception {
        HashAlgorithmProvider mockHashAlgorithmProvider = Mockito.mock(HashAlgorithmProvider.class);
        when(mockHashAlgorithmProvider.getDocumentReferenceHashAlgorithms()).thenReturn(Arrays.asList(HashAlgorithm.SHA2_384, HashAlgorithm.SHA2_512, HashAlgorithm.SHA2_512, HashAlgorithm.SHA2_256));
        TlvDocumentReference reference = new TlvDocumentReference(TEST_DOCUMENT_HELLO_TEXT, mockHashAlgorithmProvider);
        assertEquals(DOCUMENT_REFERENCE_TYPE, reference.getElementType());
        assertEquals(MIME_TYPE_APPLICATION_TXT, getMimeType(reference));
        assertEquals(TEST_FILE_NAME_TEST_TXT, getUri(reference));
        List<TLVElement> dataHashElements = reference.getRootElement().getChildElements(TLV_ELEMENT_DATA_HASH_TYPE);
        assertEquals(4, dataHashElements.size());
    }

}