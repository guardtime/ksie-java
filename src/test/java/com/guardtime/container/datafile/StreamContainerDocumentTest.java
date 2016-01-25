package com.guardtime.container.datafile;

import com.guardtime.container.AbstractBlockChainContainerTest;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.HashAlgorithm;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;

public class StreamContainerDocumentTest extends AbstractBlockChainContainerTest {

    @Test
    public void testCreateStreamBasedContainerDocumentWithoutInputStream_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Input stream must be present");
        new StreamContainerDocument(null, MIME_TYPE_APPLICATION_TXT, FILE_NAME_TEST_TXT);
    }

    @Test
    public void testCreateStreamBasedContainerDocumentWithoutMimeType_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("MIME type must be present");
        new StreamContainerDocument(new ByteArrayInputStream(TEST_DATA), null, FILE_NAME_TEST_TXT);
    }

    @Test
    public void testCreateStreamBasedContainerDocumentWithoutFileName_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("File name must be present");
        new StreamContainerDocument(new ByteArrayInputStream(TEST_DATA), MIME_TYPE_APPLICATION_TXT, null);
    }

    @Test
    public void testCreateStreamBasedContainerDocument() throws Exception {
        StreamContainerDocument document = new StreamContainerDocument(new ByteArrayInputStream(TEST_DATA), MIME_TYPE_APPLICATION_TXT, FILE_NAME_TEST_TXT);
        assertEquals(FILE_NAME_TEST_TXT, document.getFileName());
        assertEquals(MIME_TYPE_APPLICATION_TXT, document.getMimeType());
        assertEquals(Util.hash(new ByteArrayInputStream(TEST_DATA), HashAlgorithm.SHA2_256), document.getDataHash(HashAlgorithm.SHA2_256));
    }

}