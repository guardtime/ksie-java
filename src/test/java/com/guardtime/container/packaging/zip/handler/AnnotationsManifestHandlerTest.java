package com.guardtime.container.packaging.zip.handler;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AnnotationsManifestHandlerTest extends AbstractContentHandlerTest {

    private static final String VALID_ANNOTATIONS_MANIFEST_PATH = "/META-INF/annotmanifest-1.tlv";
    private static final String INVALID_ANNOTATIONS_MANIFEST_PATH = "funky_music.mp3";

    @Before
    public void setUpHandler() {
        handler = new AnnotationsManifestHandler(mockManifestFactory, mockStore);
    }

    @Test
    public void testIsSupported() throws Exception {
        assertTrue("Failed to identify supported filename string.", handler.isSupported(VALID_ANNOTATIONS_MANIFEST_PATH));
    }

    @Test
    public void testIsSupportedDoesntValidateInvalidFile() throws Exception {
        assertFalse("Identified unsupported filename string.", handler.isSupported(INVALID_ANNOTATIONS_MANIFEST_PATH));
    }
}