package com.guardtime.container.manifest.tlv;

import com.guardtime.ksi.tlv.TLVElement;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class TlvDataFilesManifestReferenceTest extends AbstractTlvManifestTest {

    @Test
    public void testCreateDataFilesManifestReference() throws Exception {
        TlvDataFilesManifest dataManifest = new TlvDataFilesManifest(asList(TEST_DOCUMENT_HELLO_TEXT));
        TlvDataFilesManifestReference reference = new TlvDataFilesManifestReference(dataManifest, TEST_FILE_NAME_TEST_TXT);
        assertEquals(DATA_MANIFEST_REFERENCE_TYPE, reference.getElementType());
        assertEquals(DATA_MANIFEST_TYPE, getMimeType(reference));
        assertEquals(TEST_FILE_NAME_TEST_TXT, getUri(reference));
    }

    @Test
    public void testReadDataFilesManifestReference() throws Exception {
        TLVElement element = createReference(DATA_MANIFEST_REFERENCE_TYPE, TEST_FILE_NAME_TEST_TXT, MIME_TYPE_APPLICATION_TXT, dataHash);
        TlvDataFilesManifestReference reference = new TlvDataFilesManifestReference(element);
        assertEquals(TEST_FILE_NAME_TEST_TXT, reference.getUri());
        assertEquals(MIME_TYPE_APPLICATION_TXT, reference.getMimeType());
        assertEquals(dataHash, reference.getHash());
    }

}