package com.guardtime.container.packaging.zip.handler;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AnnotationsManifestHandlerTest extends AbstractContentHandlerTest {

    private AnnotationsManifestHandler handler;

    @Before
    public void setUpHandler() {
        handler = new AnnotationsManifestHandler(mockManifestFactory);
    }

    @Test
    public void testIsSupported() throws Exception {
        String validString = "/META-INF/annotmanifest1.tlv_json_xml_sql";
        assertTrue("Failed to identify supported filename string.", handler.isSupported(validString));
    }

    @Test
    public void testIsSupportedDoesntValidateInvalidFile() throws Exception {
        String validString = "funky_music.mp3";
        assertFalse("Identified unsupported filename string.", handler.isSupported(validString));
    }
}