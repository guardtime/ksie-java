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

package com.guardtime.envelope.document;

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.envelope.util.Util;
import com.guardtime.ksi.hashing.HashAlgorithm;

import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;

public class StreamDocumentTest extends AbstractEnvelopeTest {

    @Test
    public void testCreateStreamBasedEnvelopeDocumentWithoutInputStream_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Input stream must be present");
        new StreamDocument(null, MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT);
    }

    @Test
    public void testCreateStreamBasedEnvelopeDocumentWithoutMimeType_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("MIME type must be present");
        new StreamDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), null, TEST_FILE_NAME_TEST_TXT);
    }

    @Test
    public void testCreateStreamBasedEnvelopeDocumentWithoutFileName_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("File name must be present");
        new StreamDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), MIME_TYPE_APPLICATION_TXT, null);
    }

    @Test
    public void testCreateStreamBasedEnvelopeDocument() throws Exception {
        try (StreamDocument document = new StreamDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT)) {
            assertEquals(TEST_FILE_NAME_TEST_TXT, document.getFileName());
            assertEquals(MIME_TYPE_APPLICATION_TXT, document.getMimeType());
            assertEquals(Util.hash(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), HashAlgorithm.SHA2_256), document.getDataHash(HashAlgorithm.SHA2_256));
        }
    }

}