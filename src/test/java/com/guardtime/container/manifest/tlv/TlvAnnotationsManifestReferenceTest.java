package com.guardtime.container.manifest.tlv;

import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TlvAnnotationsManifestReferenceTest extends AbstractTlvManifestTest {

    @Test
    public void testCreateAnnotationsManifestReference() throws Exception {
        TlvAnnotationsManifestReference reference = new TlvAnnotationsManifestReference(MOCK_URI, mockAnnotationsManifest);
        String uri = reference.getRootElement().getFirstChildElement(0x01).getDecodedString();
        DataHash hash = reference.getRootElement().getFirstChildElement(0x02).getDecodedDataHash();
        String type = reference.getRootElement().getFirstChildElement(0x03).getDecodedString();
        assertEquals(ANNOTATIONS_MANIFEST_REFERENCE_TYPE, reference.getElementType());
        assertEquals(ANNOTATION_MANIFEST_TYPE, type);
        assertEquals(MOCK_URI, uri);
        assertEquals(Util.hash(mockAnnotationsManifest.getInputStream(), HashAlgorithm.SHA2_256), hash);
    }

    @Test
    public void testReadAnnotationsManifestReference() throws Exception {
        TLVElement element = createReference(ANNOTATIONS_MANIFEST_REFERENCE_TYPE, MOCK_URI, ANNOTATION_MANIFEST_TYPE, dataHash);
        TlvAnnotationsManifestReference reference = new TlvAnnotationsManifestReference(element);
        assertEquals(MOCK_URI, reference.getUri());
        assertEquals(ANNOTATION_MANIFEST_TYPE, reference.getMimeType());
        assertEquals(dataHash, reference.getHash());
    }

}