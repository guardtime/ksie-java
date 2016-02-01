package com.guardtime.container.manifest.tlv;

import com.guardtime.ksi.tlv.TLVElement;
import com.guardtime.ksi.tlv.TLVInputStream;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class TlvAnnotationInfoManifestTest extends AbstractTlvManifestTest {
    private TlvAnnotationInfoManifest manifest;

    @Before
    public void setUpManifest() throws Exception {
        List<TLVElement> elements = new LinkedList<>();
        elements.add(TlvReferenceElementFactory.createDataManifestReferenceTlvElement(mockDataManifest));
        elements.add(TlvReferenceElementFactory.createAnnotationReferenceTlvElement(mockAnnotation));
        this.manifest = new TlvAnnotationInfoManifest(elements, "RandomStringPath");
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