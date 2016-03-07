package com.guardtime.container.packaging.zip.handler;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DataManifestHandlerTest extends AbstractContentHandlerTest {

    private static final String VALID_DATAMANIFEST_PATH = "/META-INF/datamanifest1.tlv_json_xml_bak";
    private static final String INVALID_DATAMANIFEST_PATH = "funky_music.mp3";
    private DataManifestHandler handler;

    @Before
    public void setUpHandler() {
        handler = new DataManifestHandler(mockManifestFactory);
    }

    @Test
    public void testIsSupported() throws Exception {
        assertTrue("Failed to identify supported filename string.", handler.isSupported(VALID_DATAMANIFEST_PATH));
    }

    @Test
    public void testIsSupportedDoesntValidateInvalidFile() throws Exception {
        assertFalse("Identified unsupported filename string.", handler.isSupported(INVALID_DATAMANIFEST_PATH));
    }
}