package com.guardtime.container.packaging.zip.handler;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DataFileContentHandlerTest {

    private DataFileContentHandler handler;

    @Before
    public void setUp() {
        handler = new DataFileContentHandler();
    }

    @Test
    public void testIsSupported() throws Exception {
        String validString = "important_document_is_important.doc";
        assertTrue("Failed to identify supported filename string.", handler.isSupported(validString));
    }

    @Test
    public void testIsSupportedDoesntValidateInvalidFile() throws Exception {
        String validString = "/META-INF/manifest1.tlv";
        assertFalse("Identified unsupported filename string.", handler.isSupported(validString));
    }
}