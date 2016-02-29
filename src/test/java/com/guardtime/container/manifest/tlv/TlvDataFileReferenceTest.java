package com.guardtime.container.manifest.tlv;

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.tlv.TLVElement;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TlvDataFileReferenceTest extends AbstractTlvManifestTest {

    @Test
    public void testCreateDataFileReference() throws Exception {
        TlvDataFileReference reference = new TlvDataFileReference(document);
        String uri = reference.getRootElement().getFirstChildElement(0x01).getDecodedString();
        DataHash hash = reference.getRootElement().getFirstChildElement(0x02).getDecodedDataHash();
        String mimeType = reference.getRootElement().getFirstChildElement(0x03).getDecodedString();
        assertEquals(DATA_FILE_REFERENCE_TYPE, reference.getElementType());
        assertEquals(DATA_FILE_MIME_TYPE, mimeType);
        assertEquals(DATA_FILE_NAME, uri);
        assertEquals(dataHash, hash);
    }

    @Test
    public void testReadDataFileReference() throws Exception {
        TLVElement element = createReference(DATA_FILE_REFERENCE_TYPE, DATA_FILE_NAME, DATA_FILE_MIME_TYPE, dataHash);
        TlvDataFileReference reference = new TlvDataFileReference(element);
        assertEquals(DATA_FILE_NAME, reference.getUri());
        assertEquals(DATA_FILE_MIME_TYPE, reference.getMimeType());
        assertEquals(dataHash, reference.getHash());
    }

}