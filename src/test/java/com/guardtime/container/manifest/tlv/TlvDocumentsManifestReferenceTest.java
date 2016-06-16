package com.guardtime.container.manifest.tlv;

import com.guardtime.ksi.tlv.TLVElement;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class TlvDocumentsManifestReferenceTest extends AbstractTlvManifestTest {

    @Test
    public void testCreateDocumentsManifestReference() throws Exception {
        TlvDocumentsManifest documentsManifest = new TlvDocumentsManifest(asList(TEST_DOCUMENT_HELLO_TEXT), DEFAULT_HASH_ALGORITHM);
        TlvDocumentsManifestReference reference = new TlvDocumentsManifestReference(documentsManifest, TEST_FILE_NAME_TEST_TXT, DEFAULT_HASH_ALGORITHM);
        assertEquals(DOCUMENTS_MANIFEST_REFERENCE_TYPE, reference.getElementType());
        assertEquals(DOCUMENTS_MANIFEST_TYPE, getMimeType(reference));
        assertEquals(TEST_FILE_NAME_TEST_TXT, getUri(reference));
    }

    @Test
    public void testReadDocumentsManifestReference() throws Exception {
        TLVElement element = createReference(DOCUMENTS_MANIFEST_REFERENCE_TYPE, TEST_FILE_NAME_TEST_TXT, MIME_TYPE_APPLICATION_TXT, dataHash);
        TlvDocumentsManifestReference reference = new TlvDocumentsManifestReference(element);
        assertEquals(TEST_FILE_NAME_TEST_TXT, reference.getUri());
        assertEquals(MIME_TYPE_APPLICATION_TXT, reference.getMimeType());
        assertEquals(dataHash, reference.getHashList().get(0));
    }

}