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
import com.guardtime.envelope.util.DataHashException;
import com.guardtime.envelope.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class EmptyDocumentTest extends AbstractEnvelopeTest {

    private static final String DOCUMENT_NAME = "Not_added_document_doc";
    private DataHash hash;
    private EmptyDocument document;

    @Before
    public void setUp() {
        hash = Util.hash(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)), HashAlgorithm.SHA2_256);
        document = new EmptyDocument(DOCUMENT_NAME, MIME_TYPE_APPLICATION_TXT, singletonList(hash));
    }

    @Test
    public void testGetFileName() {
        assertNotNull(document.getFileName());
    }

    @Test
    public void testGetMimeType() {
        assertEquals(MIME_TYPE_APPLICATION_TXT, document.getMimeType());
    }

    @Test
    public void testGetInputStream() throws Exception {
        try (InputStream inputStream = document.getInputStream()) {
            assertNull(inputStream);
        }
    }

    @Test
    public void testGetDataHash() throws Exception {
        assertNotNull(document.getDataHash(HashAlgorithm.SHA2_256));
    }

    @Test
    public void testIsWritable() {
        assertFalse(document.isWritable());
    }


    @Test
    public void testCreateEmptyDocumentWithoutFileName_ThrowsNullPointerException() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("File name must be present");
        new EmptyDocument(null, MIME_TYPE_APPLICATION_TXT, singletonList(hash));
    }

    @Test
    public void testCreateEmptyDocumentWithoutMimeType_ThrowsNullPointerException() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("MIME type must be present");
        new EmptyDocument(DOCUMENT_NAME, null, singletonList(hash));
    }


    @Test
    public void testCreateEmptyDocumentWithoutDataHash_ThrowsNullPointerException() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Data hash list must be present");
        new EmptyDocument(DOCUMENT_NAME, MIME_TYPE_APPLICATION_TXT, null);
    }

    @Test
    public void testGetDataHashList() throws Exception {
        List<DataHash> hashes = singletonList(hash);
        Document doc = new EmptyDocument(DOCUMENT_NAME, MIME_TYPE_APPLICATION_TXT, hashes);
        assertEquals(hashes, doc.getDataHashList(singletonList(hash.getAlgorithm())));
    }

    @Test
    public void testGetDataHashListForNotPresentAlgorithm() throws Exception {
        expectedException.expect(DataHashException.class);
        expectedException.expectMessage("Could not find any pre-generated hashes for requested algorithms!");
        List<DataHash> hashes = singletonList(Util.hash(
                new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)),
                HashAlgorithm.SHA2_256
        ));
        Document doc = new EmptyDocument(DOCUMENT_NAME, MIME_TYPE_APPLICATION_TXT, hashes);
        doc.getDataHashList(singletonList(HashAlgorithm.RIPEMD_160));
    }
}
