package com.guardtime.container.datafile;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.ksi.hashing.HashAlgorithm;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FileContainerDocumentTest extends AbstractContainerTest {

    @Test
    public void testCreateFileDocumentWithoutInputFile_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("File must be present");
        new FileContainerDocument(null, MIME_TYPE_APPLICATION_TXT);
    }

    @Test
    public void testCreateFileDocumentWithoutMimeType_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("MIME type must be present");
        new FileContainerDocument(new File(TEST_FILE_PATH_TEST_TXT), null);
    }

    @Test
    public void testCreateNewFileBasedDocument() throws Exception {
        FileContainerDocument fileDocument = new FileContainerDocument(loadFile(TEST_FILE_PATH_TEST_TXT), MIME_TYPE_APPLICATION_TXT);
        assertEquals(TEST_FILE_NAME_TEST_TXT, fileDocument.getFileName());
        assertEquals(MIME_TYPE_APPLICATION_TXT, fileDocument.getMimeType());
        assertNotNull(fileDocument.getDataHash(HashAlgorithm.SHA2_256));
    }

    @Test
    public void testOverrideDocumentName() throws Exception {
        FileContainerDocument fileDocument = new FileContainerDocument(loadFile(TEST_FILE_PATH_TEST_TXT), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_DOC);
        assertEquals(TEST_FILE_NAME_TEST_DOC, fileDocument.getFileName());
    }

}