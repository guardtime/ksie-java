package com.guardtime.container.datafile;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;

public class EmptyContainerDocumentTest extends AbstractContainerTest {

    private static final String DOCUMENT_NAME = "Not_added_document_doc";
    private DataHash hash;
    private EmptyContainerDocument document;

    @Before
    public void setUp() {
        hash = Util.hash(new ByteArrayInputStream("".getBytes()), HashAlgorithm.SHA2_256);
        document = new EmptyContainerDocument(DOCUMENT_NAME, MIME_TYPE_APPLICATION_TXT, hash);
    }

    @Test
    public void testGetFileName() throws Exception {
        assertNotNull(document.getFileName());
    }

    @Test
    public void testGetMimeType() throws Exception {
        assertEquals(MIME_TYPE_APPLICATION_TXT, document.getMimeType());
    }

    @Test
    public void testGetInputStream() throws Exception {
        assertNull(document.getInputStream());
    }

    @Test
    public void testGetDataHash() throws Exception {
        assertNotNull(document.getDataHash(HashAlgorithm.SHA2_256));
    }

    @Test
    public void testIsWritable() throws Exception {
        assertFalse(document.isWritable());
    }


    @Test
    public void testCreateEmptyDocumentWithoutFileName_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("File name must be present");
        new EmptyContainerDocument(null, MIME_TYPE_APPLICATION_TXT, hash);
    }

    @Test
    public void testCreateEmptyDocumentWithoutMimeType_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("MIME type must be present");
        new EmptyContainerDocument(DOCUMENT_NAME, null, hash);
    }


    @Test
    public void testCreateEmptyDocumentWithoutDataHash_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Data hash must be present");
        new EmptyContainerDocument(DOCUMENT_NAME, MIME_TYPE_APPLICATION_TXT, null);
    }
}