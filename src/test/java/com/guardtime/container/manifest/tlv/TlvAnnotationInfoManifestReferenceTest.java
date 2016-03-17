package com.guardtime.container.manifest.tlv;

import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TlvAnnotationInfoManifestReferenceTest extends AbstractTlvManifestTest {

    @Test
    public void testCreateAnnotationInfoManifestReference() throws Exception {
        TlvAnnotationInfoManifestReference reference = new TlvAnnotationInfoManifestReference(MOCK_URI, mockAnnotationInfoManifest, ContainerAnnotationType.FULLY_REMOVABLE);
        assertEquals(ANNOTATION_INFO_REFERENCE_TYPE, reference.getElementType());
        assertEquals(ContainerAnnotationType.FULLY_REMOVABLE.getContent(), getMimeType(reference));
        assertEquals(MOCK_URI, getUri(reference));
        assertEquals(Util.hash(mockAnnotationInfoManifest.getInputStream(), HashAlgorithm.SHA2_256), getDataHash(reference));
    }

    @Test
    public void testReadAnnotationInfoManifestReference() throws Exception {
        TLVElement element = createReference(ANNOTATION_INFO_REFERENCE_TYPE, MOCK_URI, MIME_TYPE_APPLICATION_TXT, dataHash);
        TlvAnnotationInfoManifestReference reference = new TlvAnnotationInfoManifestReference(element);
        assertEquals(MOCK_URI, reference.getUri());
        assertEquals(MIME_TYPE_APPLICATION_TXT, reference.getMimeType());
        assertEquals(dataHash, reference.getHash());
    }

}