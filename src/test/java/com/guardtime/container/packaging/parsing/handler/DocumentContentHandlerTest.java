package com.guardtime.container.packaging.parsing.handler;

import org.junit.Before;
import org.junit.Test;

import static com.guardtime.container.packaging.MimeType.MIME_TYPE_ENTRY_NAME;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DocumentContentHandlerTest extends AbstractContentHandlerTest {

    private static final String VALID_DOCUMENT_PATH = "important_document_is_important.doc";
    private static final String INVALID_DOCUMENT_PATH = "/META-INF/manifest-1.tlv";

    @Before
    public void setUpHandler() {
        handler = new DocumentContentHandler(store);
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
        assertFalse("Identified MIME_TYPE filename string.", handler.isSupported(MIME_TYPE_ENTRY_NAME));
    }
}