package com.guardtime.container.manifest.tlv;

import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TlvAnnotationInfoManifestReferenceTest extends AbstractTlvManifestTest {

    @Test
    public void testCreateAnnotationInfoManifestReference() throws Exception {
        TlvAnnotationInfoManifestReference reference = new TlvAnnotationInfoManifestReference(MOCK_URI, mockAnnotationInfoManifest, ContainerAnnotationType.FULLY_REMOVABLE);
        String uri = reference.getRootElement().getFirstChildElement(0x01).getDecodedString();
        DataHash hash = reference.getRootElement().getFirstChildElement(0x02).getDecodedDataHash();
        String mimeType = reference.getRootElement().getFirstChildElement(0x03).getDecodedString();
        assertEquals(ANNOTATION_INFO_REFERENCE_TYPE, reference.getElementType());
        assertEquals(ContainerAnnotationType.FULLY_REMOVABLE.getContent(), mimeType);
        assertEquals(MOCK_URI, uri);
        assertEquals(Util.hash(mockAnnotationInfoManifest.getInputStream(), HashAlgorithm.SHA2_256), hash);
    }

    @Test
    public void testReadAnnotationInfoManifestReference() throws Exception {
        TLVElement element = createReference(ANNOTATION_INFO_REFERENCE_TYPE, MOCK_URI, DATA_FILE_MIME_TYPE, dataHash);
        TlvAnnotationInfoManifestReference reference = new TlvAnnotationInfoManifestReference(element);
        assertEquals(MOCK_URI, reference.getUri());
        assertEquals(DATA_FILE_MIME_TYPE, reference.getMimeType());
        assertEquals(dataHash, reference.getHash());
    }

}