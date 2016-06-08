package com.guardtime.container.manifest.tlv;

import com.guardtime.container.util.Pair;
import com.guardtime.ksi.tlv.TLVElement;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TlvAnnotationDataReferenceTest extends AbstractTlvManifestTest {

    @Test
    public void testCreateAnnotationReference() throws Exception {
        TlvAnnotationDataReference reference = new TlvAnnotationDataReference(Pair.of(MOCK_URI, mockAnnotation), DEFAULT_HASH_ALGORITHM);
        assertEquals(ANNOTATION_REFERENCE_TYPE, reference.getElementType());
        assertEquals(ANNOTATION_DOMAIN_COM_GUARDTIME, getDomain(reference));
        assertEquals(MOCK_URI, getUri(reference));
        assertEquals(dataHash, getDataHash(reference));
    }

    @Test
    public void testReadAnnotationReference() throws Exception {
        TLVElement element = createAnnotationReferenceElement();
        TlvAnnotationDataReference reference = new TlvAnnotationDataReference(element);
        assertEquals(MOCK_URI, reference.getUri());
        assertEquals(ANNOTATION_DOMAIN_COM_GUARDTIME, reference.getDomain());
        assertEquals(dataHash, reference.getHash());
    }

}