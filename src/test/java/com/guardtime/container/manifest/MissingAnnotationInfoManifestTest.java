package com.guardtime.container.manifest;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNull;

public class MissingAnnotationInfoManifestTest {

    AnnotationInfoManifest manifest;

    @Before
    public void setUp() throws Exception {
        manifest = new MissingAnnotationInfoManifest();
    }

    @Test
    public void testGetAnnotationReference() throws Exception {
        assertNull(manifest.getAnnotationReference());
    }

    @Test
    public void testGetDataManifestReference() throws Exception {
        assertNull(manifest.getDataManifestReference());
    }

    @Test
    public void testGetInputStream() throws Exception {
        assertNull(manifest.getInputStream());
    }
}