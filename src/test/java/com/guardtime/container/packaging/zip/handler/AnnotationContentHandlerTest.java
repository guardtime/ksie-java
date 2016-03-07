package com.guardtime.container.packaging.zip.handler;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AnnotationContentHandlerTest {

    private static final String VALID_ANNOTATION_PATH = "/META-INF/annotation1.dat";
    private static final String INVALID_ANNOTATION_PATH = "funky_music.mp3";
    private AnnotationContentHandler handler;

    @Before
    public void setUp() {
        handler = new AnnotationContentHandler();
    }

    @Test
    public void testIsSupported() throws Exception {
        assertTrue("Failed to identify supported filename string.", handler.isSupported(VALID_ANNOTATION_PATH));
    }

    @Test
    public void testIsSupportedDoesntValidateInvalidFile() throws Exception {
        assertFalse("Identified unsupported filename string.", handler.isSupported(INVALID_ANNOTATION_PATH));
    }
}