package com.guardtime.container.manifest.tlv;

import com.guardtime.ksi.tlv.TLVInputStream;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

public class TlvAnnotationInfoManifestTest extends AbstractTlvManifestTest {
    private TlvAnnotationInfoManifest manifest;

    @Before
    public void setUpManifest() throws Exception {
        this.manifest = new TlvAnnotationInfoManifest(mockAnnotation, mockDataManifest, "RandomStringPath");
    }

    @Test
    public void testInputStreamTlvElementExistence() throws Exception {
        InputStream is = manifest.getInputStream();
        testMagic(is, ANNOTATION_INFO_MANIFEST_MAGIC);

        TLVInputStream tlvInputStream = new TLVInputStream(is);
        testTlvElement(tlvInputStream, DATA_MANIFEST_REFERENCE_TYPE);
        testTlvElement(tlvInputStream, ANNOTATION_REFERENCE_TYPE);
    }

}