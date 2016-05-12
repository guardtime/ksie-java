package com.guardtime.container.packaging.zip.handler;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DocumentsManifestHandlerTest extends AbstractContentHandlerTest {

    private static final String VALID_DATA_FILES_MANIFEST_PATH = "/META-INF/datamanifest1.tlv";
    private static final String INVALID_DATA_FILES_MANIFEST_PATH = "funky_music.mp3";
    private DocumentsManifestHandler handler;

    @Before
    public void setUpHandler() {
        handler = new DocumentsManifestHandler(mockManifestFactory);
    }

    @Test
    public void testIsSupported() throws Exception {
        assertTrue("Failed to identify supported filename string.", handler.isSupported(VALID_DATA_FILES_MANIFEST_PATH));
    }

    @Test
    public void testIsSupportedDoesntValidateInvalidFile() throws Exception {
        assertFalse("Identified unsupported filename string.", handler.isSupported(INVALID_DATA_FILES_MANIFEST_PATH));
    }
}