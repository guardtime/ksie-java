package com.guardtime.container.manifest.tlv;

import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TlvAnnotationsManifestReferenceTest extends AbstractTlvManifestTest {

    @Test
    public void testCreateAnnotationsManifestReference() throws Exception {
        TlvAnnotationsManifestReference reference = new TlvAnnotationsManifestReference(MOCK_URI, mockAnnotationsManifest);
        assertEquals(ANNOTATIONS_MANIFEST_REFERENCE_TYPE, reference.getElementType());
        assertEquals(ANNOTATION_MANIFEST_TYPE, getMimeType(reference));
        assertEquals(MOCK_URI, getUri(reference));
        assertEquals(Util.hash(mockAnnotationsManifest.getInputStream(), HashAlgorithm.SHA2_256), getDataHash(reference));
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