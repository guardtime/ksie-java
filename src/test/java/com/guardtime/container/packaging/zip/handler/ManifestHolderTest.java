package com.guardtime.container.packaging.zip.handler;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ManifestHolderTest extends AbstractContentHandlerTest {

    private static final String VALID_MANIFEST_PATH = "/META-INF/manifest1.tlv_json_xml_bak";
    private static final String INVALID_MANIFEST_PATH = "funky_music.mp3";
    private ManifestHolder handler;

    @Before
    public void setUpHandler() {
        handler = new ManifestHolder(mockManifestFactory);
    }

    @Test
    public void testIsSupported() throws Exception {
        assertTrue("Failed to identify supported filename string.", handler.isSupported(VALID_MANIFEST_PATH));
    }

    @Test
    public void testIsSupportedDoesntValidateInvalidFile() throws Exception {
        assertFalse("Identified unsupported filename string.", handler.isSupported(INVALID_MANIFEST_PATH));
    }
}