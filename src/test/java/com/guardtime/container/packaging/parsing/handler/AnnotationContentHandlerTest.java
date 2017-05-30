package com.guardtime.container.packaging.parsing.handler;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AnnotationContentHandlerTest extends AbstractContentHandlerTest {

    private static final String VALID_ANNOTATION_PATH = "/META-INF/annotation-1.dat";
    private static final String INVALID_ANNOTATION_PATH = "funky_music.mp3";

    @Before
    public void setUpHandler() {
        handler = new AnnotationContentHandler(store);
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