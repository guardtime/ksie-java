package com.guardtime.container.manifest.tlv;

import com.guardtime.container.util.Pair;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.HashAlgorithm;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TlvAnnotationInfoManifestTest extends AbstractTlvManifestTest {

    private static final String ANNOTATIO_URI = "annotatio_uri";

    @Test
    public void testCreateAnnotationInfoManifest() throws Exception {
        TlvAnnotationInfoManifest annotationManifest = new TlvAnnotationInfoManifest(Pair.of(ANNOTATIO_URI, mockAnnotation), Pair.of("", mockDataManifest));

        assertNotNull(annotationManifest.getAnnotationReference());
        assertNotNull(annotationManifest.getDataManifestReference());
        assertEquals(ANNOTATION_DOMAIN, annotationManifest.getAnnotationReference().getDomain());
        //TODO hashes
    }

    //TODO read manifest
}