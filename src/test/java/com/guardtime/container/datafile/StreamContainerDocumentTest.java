package com.guardtime.container.datafile;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.HashAlgorithm;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;

public class StreamContainerDocumentTest extends AbstractContainerTest {

    @Test
    public void testCreateStreamBasedContainerDocumentWithoutInputStream_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Input stream must be present");
        new StreamContainerDocument(null, MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT);
    }

    @Test
    public void testCreateStreamBasedContainerDocumentWithoutMimeType_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("MIME type must be present");
        new StreamContainerDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), null, TEST_FILE_NAME_TEST_TXT);
    }

    @Test
    public void testCreateStreamBasedContainerDocumentWithoutFileName_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("File name must be present");
        new StreamContainerDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), MIME_TYPE_APPLICATION_TXT, null);
    }

    @Test
    public void testCreateStreamBasedContainerDocument() throws Exception {
        StreamContainerDocument document = new StreamContainerDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT);
        assertEquals(TEST_FILE_NAME_TEST_TXT, document.getFileName());
        assertEquals(MIME_TYPE_APPLICATION_TXT, document.getMimeType());
        assertEquals(Util.hash(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), HashAlgorithm.SHA2_256), document.getDataHash(HashAlgorithm.SHA2_256));
    }

}