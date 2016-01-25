package com.guardtime.container.manifest.tlv;

import com.guardtime.ksi.tlv.TLVInputStream;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.LinkedList;

public class TlvAnnotationsManifestTest extends AbstractTlvManifestTest {
    private TlvAnnotationsManifest manifest;

    @Before
    public void setupManifest(){
        LinkedList<TlvAnnotationInfoManifest> annotationManifests = new LinkedList<>();
        annotationManifests.add(mockAnnotationInfoManifest);
        this.manifest = new TlvAnnotationsManifest(annotationManifests, "Non-important-for-test");
    }

    @Test
    public void testInputStreamTlvElementExistence() throws Exception {
        InputStream is = manifest.getInputStream();
        testMagic(is, ANNOTATIONS_MANIFEST_MAGIC);

        testTlvElement(new TLVInputStream(is), ANNOTATION_INFO_REFERENCE_TYPE);
    }
}