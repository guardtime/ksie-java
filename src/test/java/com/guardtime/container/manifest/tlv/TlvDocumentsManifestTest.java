package com.guardtime.container.manifest.tlv;

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class TlvDocumentsManifestTest extends AbstractTlvManifestTest {

    @Test
    public void testCreateDocumentsManifest() throws Exception {
        TlvDocumentsManifest documentsManifest = new TlvDocumentsManifest(Collections.singletonList(TEST_DOCUMENT_HELLO_TEXT), DEFAULT_HASH_ALGORITHM_PROVIDER);
        try (InputStream is = documentsManifest.getInputStream()) {
            testMagic(is, DOCUMENTS_MANIFEST_MAGIC);
            assertEquals(1, documentsManifest.getDocumentReferences().size());
            TlvDocumentReference reference = documentsManifest.getDocumentReferences().get(0);
            assertEquals(TEST_FILE_NAME_TEST_TXT, reference.getUri());
            assertEquals(MIME_TYPE_APPLICATION_TXT, reference.getMimeType());
            assertEquals(DOCUMENT_REFERENCE_TYPE, reference.getElementType());
        }
    }

    @Test
    public void testReadDocumentsManifest() throws Exception {
        TLVElement reference = createReference(DOCUMENT_REFERENCE_TYPE, TEST_FILE_NAME_TEST_TXT, MIME_TYPE_APPLICATION_TXT, new DataHash(HashAlgorithm.SHA2_256, new byte[32]));
        byte[] bytes = join(DOCUMENTS_MANIFEST_MAGIC, reference.getEncoded());
        TlvDocumentsManifest documentsManifest = new TlvDocumentsManifest(new ByteArrayInputStream(bytes));
        try (InputStream is = documentsManifest.getInputStream()) {
            testMagic(is, DOCUMENTS_MANIFEST_MAGIC);
            assertEquals(1, documentsManifest.getDocumentReferences().size());
            TlvDocumentReference ref = documentsManifest.getDocumentReferences().get(0);
            assertEquals(TEST_FILE_NAME_TEST_TXT, ref.getUri());
            assertEquals(MIME_TYPE_APPLICATION_TXT, ref.getMimeType());
            assertEquals(DOCUMENT_REFERENCE_TYPE, ref.getElementType());
        }
    }

}