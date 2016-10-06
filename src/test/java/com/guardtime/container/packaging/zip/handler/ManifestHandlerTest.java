package com.guardtime.container.packaging.zip.handler;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;

import static org.junit.Assert.*;

public class ManifestHandlerTest extends AbstractContentHandlerTest {

    private static final String VALID_MANIFEST_PATH = "/META-INF/manifest-7.tlv";
    private static final String INVALID_MANIFEST_PATH = "funky_music.mp3";
    private ManifestHandler handler;

    @Mock
    private File mockFile;

    @Before
    public void setUpHandler() {
        handler = new ManifestHandler(mockManifestFactory);
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