package com.guardtime.container.packaging.zip.handler;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ManifestHolderTest extends AbstractContentHandlerTest {

    private ManifestHolder handler;

    @Before
    public void setUpHandler() {
        handler = new ManifestHolder(mockManifestFactory);
    }

    @Test
    public void testIsSupported() throws Exception {
        String validString = "/META-INF/manifest1.tlv_json_xml_bak";
        assertTrue("Failed to identify supported filename string.", handler.isSupported(validString));
    }

    @Test
    public void testIsSupportedDoesntValidateInvalidFile() throws Exception {
        String validString = "funky_music.mp3";
        assertFalse("Identified unsupported filename string.", handler.isSupported(validString));
    }
}