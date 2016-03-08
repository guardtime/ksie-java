package com.guardtime.container.packaging.zip.handler;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AnnotationManifestHandlerTest extends AbstractContentHandlerTest {

    private static final String VALID_ANNOTATION_MANIFEST_PATH = "/META-INF/annotation1.tlv";
    private static final String INVALID_ANNOTATION_MANIFEST_PATH = "funky_music.mp3";
    private AnnotationManifestHandler handler;

    @Before
    public void setUpHandler() {
        handler = new AnnotationManifestHandler(mockManifestFactory);
    }

    @Test
    public void testIsSupported() throws Exception {
        assertTrue("Failed to identify supported filename string.", handler.isSupported(VALID_ANNOTATION_MANIFEST_PATH));
    }

    @Test
    public void testIsSupportedDoesntValidateInvalidFile() throws Exception {
        assertFalse("Identified unsupported filename string.", handler.isSupported(INVALID_ANNOTATION_MANIFEST_PATH));
    }
}