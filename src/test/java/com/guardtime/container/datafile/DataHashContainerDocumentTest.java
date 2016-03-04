package com.guardtime.container.datafile;

import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;

public class DataHashContainerDocumentTest {

    private static final String APPLICATION_TEXT = "application/text";
    private DataHashContainerDocument document;

    @Before
    public void setUp() {
        DataHash hash = Util.hash(new ByteArrayInputStream("".getBytes()), HashAlgorithm.SHA2_256);
        document = new DataHashContainerDocument(APPLICATION_TEXT, hash);
    }

    @Test
    public void testGetFileName() throws Exception {
        assertNull(document.getFileName());
    }

    @Test
    public void testGetMimeType() throws Exception {
        assertEquals(APPLICATION_TEXT, document.getMimeType());
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
}