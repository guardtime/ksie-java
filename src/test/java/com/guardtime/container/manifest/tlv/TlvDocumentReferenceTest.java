package com.guardtime.container.manifest.tlv;

import com.guardtime.ksi.tlv.TLVElement;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TlvDocumentReferenceTest extends AbstractTlvManifestTest {

    @Test
    public void testCreateDocumentReference() throws Exception {
        TlvDocumentReference reference = new TlvDocumentReference(TEST_DOCUMENT_HELLO_TEXT, DEFAULT_HASH_ALGORITHM);
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
        assertEquals(dataHash, reference.getHash());
    }

}