package com.guardtime.container.manifest.tlv;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.ksi.tlv.TLVInputStream;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class TlvAnnotationsManifestTest extends AbstractTlvManifestTest {
    private TlvAnnotationsManifest manifest;

    @Before
    public void setupManifest() throws BlockChainContainerException {
        Map<ContainerAnnotation, TlvAnnotationInfoManifest> map = new HashMap<>();
        map.put(mockAnnotation, mockAnnotationInfoManifest);
        this.manifest = new TlvAnnotationsManifest(map, "Non-important-for-test");
    }

    @Test
    public void testInputStreamTlvElementExistence() throws Exception {
        InputStream is = manifest.getInputStream();
        testMagic(is, ANNOTATIONS_MANIFEST_MAGIC);

        testTlvElement(new TLVInputStream(is), ANNOTATION_INFO_REFERENCE_TYPE);
    }
}