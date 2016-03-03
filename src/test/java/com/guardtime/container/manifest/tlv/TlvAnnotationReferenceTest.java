package com.guardtime.container.manifest.tlv;

import com.guardtime.container.util.Pair;
import com.guardtime.ksi.tlv.TLVElement;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TlvAnnotationReferenceTest extends AbstractTlvManifestTest {

    @Test
    public void testCreateAnnotationReference() throws Exception {
        TlvAnnotationReference reference = new TlvAnnotationReference(Pair.of(MOCK_URI, mockAnnotation));
        assertEquals(ANNOTATION_REFERENCE_TYPE, reference.getElementType());
        assertEquals(ANNOTATION_DOMAIN_COM_GUARDTIME, getDomain(reference));
        assertEquals(MOCK_URI, getUri(reference));
        assertEquals(dataHash, getDataHash(reference));
    }

    @Test
    public void testReadAnnotationReference() throws Exception {
        TLVElement element = createAnnotationReferenceElement();
        TlvAnnotationReference reference = new TlvAnnotationReference(element);
        assertEquals(MOCK_URI, reference.getUri());
        assertEquals(ANNOTATION_DOMAIN_COM_GUARDTIME, reference.getDomain());
        assertEquals(dataHash, reference.getHash());
    }

}