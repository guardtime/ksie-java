package com.guardtime.container.manifest.tlv;

import com.guardtime.ksi.tlv.TLVInputStream;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

public class TlvSignatureManifestTest extends AbstractTlvManifestTest {
    private TlvSignatureManifest manifest;

    @Before
    public void setUpManifest() {
        this.manifest = new TlvSignatureManifest(mockDataManifest, mockAnnotationsManifest, "Non-important-for-test");
    }

    @Test
    public void testInputStreamTlvElementExistence() throws Exception {
        InputStream is = manifest.getInputStream();
        testMagic(is, SIGNATURE_MANIFEST_MAGIC);

        TLVInputStream tlvInputStream = new TLVInputStream(is);
        testTlvElement(tlvInputStream, DATA_MANIFEST_REFERENCE_TYPE);
        testTlvElement(tlvInputStream, SIGNATURE_REFERENCE_TYPE);
        testTlvElement(tlvInputStream, ANNOTATIONS_MANIFEST_REFERENCE_TYPE);

    }
}