package com.guardtime.container.manifest.tlv;

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static java.util.Arrays.asList;

public class TlvDataFilesManifestTest extends AbstractTlvManifestTest {

    @Test
    public void testCreateDataFilesManifest() throws Exception {
        TlvDataFilesManifest dataManifest = new TlvDataFilesManifest(asList(TEST_DOCUMENT_HELLO_TEXT));
        InputStream is = dataManifest.getInputStream();
        testMagic(is, DATA_FILES_MANIFEST_MAGIC);
        Assert.assertEquals(1, dataManifest.getDataFileReferences().size());
        TlvDataFileReference reference = dataManifest.getDataFileReferences().get(0);
        Assert.assertEquals(TEST_FILE_NAME_TEST_TXT, reference.getUri());
        Assert.assertEquals(MIME_TYPE_APPLICATION_TXT, reference.getMimeType());
        Assert.assertEquals(DATA_FILE_REFERENCE_TYPE, reference.getElementType());
    }

    @Test
    public void testReadDataFilesManifest() throws Exception {
        TLVElement reference = createReference(DATA_FILE_REFERENCE_TYPE, TEST_FILE_NAME_TEST_TXT, MIME_TYPE_APPLICATION_TXT, new DataHash(HashAlgorithm.SHA2_256, new byte[32]));
        byte[] bytes = join(DATA_FILES_MANIFEST_MAGIC, reference.getEncoded());
        TlvDataFilesManifest dataManifest = new TlvDataFilesManifest(new ByteArrayInputStream(bytes));
        InputStream is = dataManifest.getInputStream();
        testMagic(is, DATA_FILES_MANIFEST_MAGIC);
        Assert.assertEquals(1, dataManifest.getDataFileReferences().size());
        TlvDataFileReference ref = dataManifest.getDataFileReferences().get(0);
        Assert.assertEquals(TEST_FILE_NAME_TEST_TXT, ref.getUri());
        Assert.assertEquals(MIME_TYPE_APPLICATION_TXT, ref.getMimeType());
        Assert.assertEquals(DATA_FILE_REFERENCE_TYPE, ref.getElementType());
    }

}