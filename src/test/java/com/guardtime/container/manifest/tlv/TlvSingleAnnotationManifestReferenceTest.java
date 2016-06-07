package com.guardtime.container.manifest.tlv;

import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TlvSingleAnnotationManifestReferenceTest extends AbstractTlvManifestTest {

    @Test
    public void testCreateSingleAnnotationManifestReference() throws Exception {
        TlvSingleAnnotationManifestReference reference = new TlvSingleAnnotationManifestReference(MOCK_URI, mockSingleAnnotationManifest, ContainerAnnotationType.FULLY_REMOVABLE);
        assertEquals(ANNOTATION_INFO_REFERENCE_TYPE, reference.getElementType());
        assertEquals(ContainerAnnotationType.FULLY_REMOVABLE.getContent(), getMimeType(reference));
        assertEquals(MOCK_URI, getUri(reference));
        assertEquals(Util.hash(mockSingleAnnotationManifest.getInputStream(), HashAlgorithm.SHA2_256), getDataHash(reference));
    }

    @Test
    public void testReadSingleAnnotationManifestReference() throws Exception {
        TLVElement element = createReference(ANNOTATION_INFO_REFERENCE_TYPE, MOCK_URI, MIME_TYPE_APPLICATION_TXT, dataHash);
        TlvSingleAnnotationManifestReference reference = new TlvSingleAnnotationManifestReference(element);
        assertEquals(MOCK_URI, reference.getUri());
        assertEquals(MIME_TYPE_APPLICATION_TXT, reference.getMimeType());
        assertEquals(dataHash, reference.getHashList().get(0));
    }

}