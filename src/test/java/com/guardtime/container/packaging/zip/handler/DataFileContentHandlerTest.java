package com.guardtime.container.packaging.zip.handler;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DataFileContentHandlerTest {

    private static final String VALID_DOCUMENT_PATH = "important_document_is_important.doc";
    private static final String INVALID_DOCUMENT_PATH = "/META-INF/manifest1.tlv";
    private DataFileContentHandler handler;

    @Before
    public void setUp() {
        handler = new DataFileContentHandler();
    }

    @Test
    public void testIsSupported() throws Exception {
        assertTrue("Failed to identify supported filename string.", handler.isSupported(VALID_DOCUMENT_PATH));
    }

    @Test
    public void testIsSupportedDoesntValidateInvalidFile() throws Exception {
        assertFalse("Identified unsupported filename string.", handler.isSupported(INVALID_DOCUMENT_PATH));
    }
}