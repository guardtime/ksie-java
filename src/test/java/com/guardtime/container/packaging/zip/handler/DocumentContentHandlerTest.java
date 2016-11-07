package com.guardtime.container.packaging.zip.handler;

import org.junit.Before;
import org.junit.Test;

import static com.guardtime.container.packaging.zip.ZipContainerPackagingFactory.MIME_TYPE_ENTRY_NAME;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DocumentContentHandlerTest {

    private static final String VALID_DOCUMENT_PATH = "important_document_is_important.doc";
    private static final String INVALID_DOCUMENT_PATH = "/META-INF/manifest-1.tlv";
    private DocumentContentHandler handler;

    @Before
    public void setUp() {
        handler = new DocumentContentHandler();
    }

    @Test
    public void testIsSupported() throws Exception {
        assertTrue("Failed to identify supported filename string.", handler.isSupported(VALID_DOCUMENT_PATH));
    }

    @Test
    public void testIsSupportedDoesntValidateInvalidFile() throws Exception {
        assertFalse("Identified unsupported filename string.", handler.isSupported(INVALID_DOCUMENT_PATH));
    }

    @Test
    public void testIsSupportedDoesNotValidateMimetypeFile() throws Exception {
        assertFalse("Identified MIMETYPE filename string.", handler.isSupported(MIME_TYPE_ENTRY_NAME));
    }
}