/*
 * Copyright 2013-2017 Guardtime, Inc.
 *
 * This file is part of the Guardtime client SDK.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES, CONDITIONS, OR OTHER LICENSES OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * "Guardtime" and "KSI" are trademarks or registered trademarks of
 * Guardtime, Inc., and no license to trademarks is granted; Guardtime
 * reserves and retains all trademark rights.
 */

package com.guardtime.container.document;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.ksi.hashing.HashAlgorithm;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void testCloseDoesNotDeleteFile() throws Exception {
        File file = loadFile(TEST_FILE_PATH_TEST_TXT);
        FileContainerDocument fileDocument = new FileContainerDocument(file, MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_DOC);
        fileDocument.close();
        assertTrue(file.exists());
    }

}