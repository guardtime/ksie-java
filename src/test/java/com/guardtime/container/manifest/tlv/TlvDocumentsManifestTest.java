package com.guardtime.container.manifest.tlv;

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static java.util.Arrays.asList;

public class TlvDocumentsManifestTest extends AbstractTlvManifestTest {

    @Test
    public void testCreateDocumentsManifest() throws Exception {
        TlvDocumentsManifest documentsManifest = new TlvDocumentsManifest(asList(TEST_DOCUMENT_HELLO_TEXT), DEFAULT_HASH_ALGORITHM_PROVIDER);
        InputStream is = documentsManifest.getInputStream();
        testMagic(is, DOCUMENTS_MANIFEST_MAGIC);
        Assert.assertEquals(1, documentsManifest.getDocumentReferences().size());
        TlvDocumentReference reference = documentsManifest.getDocumentReferences().get(0);
        Assert.assertEquals(TEST_FILE_NAME_TEST_TXT, reference.getUri());
        Assert.assertEquals(MIME_TYPE_APPLICATION_TXT, reference.getMimeType());
        Assert.assertEquals(DOCUMENT_REFERENCE_TYPE, reference.getElementType());
    }

    @Test
    public void testReadDocumentsManifest() throws Exception {
        TLVElement reference = createReference(DOCUMENT_REFERENCE_TYPE, TEST_FILE_NAME_TEST_TXT, MIME_TYPE_APPLICATION_TXT, new DataHash(HashAlgorithm.SHA2_256, new byte[32]));
        byte[] bytes = join(DOCUMENTS_MANIFEST_MAGIC, reference.getEncoded());
        TlvDocumentsManifest documentsManifest = new TlvDocumentsManifest(new ByteArrayInputStream(bytes));
        InputStream is = documentsManifest.getInputStream();
        testMagic(is, DOCUMENTS_MANIFEST_MAGIC);
        Assert.assertEquals(1, documentsManifest.getDocumentReferences().size());
        TlvDocumentReference ref = documentsManifest.getDocumentReferences().get(0);
        Assert.assertEquals(TEST_FILE_NAME_TEST_TXT, ref.getUri());
        Assert.assertEquals(MIME_TYPE_APPLICATION_TXT, ref.getMimeType());
        Assert.assertEquals(DOCUMENT_REFERENCE_TYPE, ref.getElementType());
    }

}