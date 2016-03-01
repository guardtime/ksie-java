package com.guardtime.container.manifest.tlv;

import com.guardtime.container.util.Pair;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.tlv.TLVElement;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TlvAnnotationReferenceTest extends AbstractTlvManifestTest {

    @Test
    public void testCreateAnnotationReference() throws Exception {
        TlvAnnotationReference reference = new TlvAnnotationReference(Pair.of(MOCK_URI, mockAnnotation));
        String uri = reference.getRootElement().getFirstChildElement(0x01).getDecodedString();
        DataHash hash = reference.getRootElement().getFirstChildElement(0x02).getDecodedDataHash();
        String domain = reference.getRootElement().getFirstChildElement(0x04).getDecodedString();
        assertEquals(ANNOTATION_REFERENCE_TYPE, reference.getElementType());
        assertEquals(ANNOTATION_DOMAIN, domain);
        assertEquals(MOCK_URI, uri);
        assertEquals(dataHash, hash);
    }

    @Test
    public void testReadAnnotationReference() throws Exception {
        TLVElement element = createReference(ANNOTATION_REFERENCE_TYPE, MOCK_URI, null, dataHash);
        TLVElement domainElement = new TLVElement(false, false, 0x04);
        domainElement.setStringContent(ANNOTATION_DOMAIN);
        element.addChildElement(domainElement);
        TlvAnnotationReference reference = new TlvAnnotationReference(element);
        assertEquals(MOCK_URI, reference.getUri());
        assertEquals(ANNOTATION_DOMAIN, reference.getDomain());
        assertEquals(dataHash, reference.getHash());
    }

}