package com.guardtime.container.packaging.zip.handler;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AnnotationContentHandlerTest {

    private AnnotationContentHandler handler;

    @Before
    public void setUp() {
        handler = new AnnotationContentHandler();
    }

    @Test
    public void testIsSupported() throws Exception {
        String validString = "/META-INF/annotation1.dat";
        assertTrue("Failed to identify supported filename string.", handler.isSupported(validString));
    }

    @Test
    public void testIsSupportedDoesntValidateInvalidFile() throws Exception {
        String validString = "funky_music.mp3";
        assertFalse("Identified unsupported filename string.", handler.isSupported(validString));
    }
}