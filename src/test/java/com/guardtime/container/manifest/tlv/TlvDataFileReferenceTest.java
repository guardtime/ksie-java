package com.guardtime.container.manifest.tlv;

import com.guardtime.ksi.tlv.TLVElement;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TlvDataFileReferenceTest extends AbstractTlvManifestTest {

    @Test
    public void testCreateDataFileReference() throws Exception {
        TlvDataFileReference reference = new TlvDataFileReference(TEST_DOCUMENT_HELLO_TEXT);
        assertEquals(DATA_FILE_REFERENCE_TYPE, reference.getElementType());
        assertEquals(MIME_TYPE_APPLICATION_TXT, getMimeType(reference));
        assertEquals(TEST_FILE_NAME_TEST_TXT, getUri(reference));
        assertEquals(dataHash, getDataHash(reference));
    }

    @Test
    public void testReadDataFileReference() throws Exception {
        TLVElement element = createReference(DATA_FILE_REFERENCE_TYPE, TEST_FILE_NAME_TEST_TXT, MIME_TYPE_APPLICATION_TXT, dataHash);
        TlvDataFileReference reference = new TlvDataFileReference(element);
        assertEquals(TEST_FILE_NAME_TEST_TXT, reference.getUri());
        assertEquals(MIME_TYPE_APPLICATION_TXT, reference.getMimeType());
        assertEquals(dataHash, reference.getHash());
    }

}