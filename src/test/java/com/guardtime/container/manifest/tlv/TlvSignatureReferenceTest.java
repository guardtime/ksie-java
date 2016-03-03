package com.guardtime.container.manifest.tlv;

import com.guardtime.ksi.tlv.TLVElement;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TlvSignatureReferenceTest extends AbstractTlvManifestTest {

    @Test
    public void testCreateSignatureReference() throws Exception {
        TlvSignatureReference reference = new TlvSignatureReference(SIGNATURE_URI, SIGNATURE_TYPE);
        String uri = reference.getRootElement().getFirstChildElement(0x01).getDecodedString();
        String mimeType = reference.getRootElement().getFirstChildElement(0x03).getDecodedString();
        assertEquals(SIGNATURE_REFERENCE_TYPE, reference.getElementType());
        assertEquals(SIGNATURE_URI, uri);
        assertEquals(SIGNATURE_TYPE, mimeType);
    }

    @Test
    public void testReadSignatureReference() throws Exception {
        TLVElement element = createReference(SIGNATURE_REFERENCE_TYPE, SIGNATURE_URI, SIGNATURE_TYPE, null);
        TlvSignatureReference signatureReference = new TlvSignatureReference(element);
        assertEquals(SIGNATURE_URI, signatureReference.getUri());
        assertEquals(SIGNATURE_TYPE, signatureReference.getType());
    }

}