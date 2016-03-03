package com.guardtime.container.manifest.tlv;

import com.guardtime.container.manifest.ContainerManifestMimeType;
import com.guardtime.ksi.tlv.TLVElement;

import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class TlvDataFilesManifestReferenceTest extends AbstractTlvManifestTest {

    @Test
    public void testCreateDataFilesManifestReference() throws Exception {
        TlvDataFilesManifest dataManifest = new TlvDataFilesManifest(asList(document));
        TlvDataFilesManifestReference reference = new TlvDataFilesManifestReference(dataManifest, DATA_FILE_NAME);
        String uri = reference.getRootElement().getFirstChildElement(0x01).getDecodedString();
        String mimeType = reference.getRootElement().getFirstChildElement(0x03).getDecodedString();
        assertEquals(DATA_MANIFEST_REFERENCE_TYPE, reference.getElementType());
        assertEquals(ContainerManifestMimeType.DATA_MANIFEST.getType(), mimeType);
        assertEquals(DATA_FILE_NAME, uri);
    }

    @Test
    public void testReadDataFilesManifestReference() throws Exception {
        TLVElement element = createReference(DATA_MANIFEST_REFERENCE_TYPE, DATA_FILE_NAME, DATA_FILE_TYPE, dataHash);
        TlvDataFilesManifestReference reference = new TlvDataFilesManifestReference(element);
        assertEquals(DATA_FILE_NAME, reference.getUri());
        assertEquals(DATA_FILE_TYPE, reference.getMimeType());
        assertEquals(dataHash, reference.getHash());
    }

}