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
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class EmptyEnvelopeDocumentTest extends AbstractEnvelopeTest {

    private static final String DOCUMENT_NAME = "Not_added_document_doc";
    private DataHash hash;
    private EmptyEnvelopeDocument document;

    @Before
    public void setUp() {
        hash = Util.hash(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)), HashAlgorithm.SHA2_256);
        document = new EmptyEnvelopeDocument(DOCUMENT_NAME, MIME_TYPE_APPLICATION_TXT, Collections.singletonList(hash));
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
        try (InputStream inputStream = document.getInputStream()) {
            assertNull(inputStream);
        }
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
        new EmptyEnvelopeDocument(null, MIME_TYPE_APPLICATION_TXT, Collections.singletonList(hash));
    }

    @Test
    public void testCreateEmptyDocumentWithoutMimeType_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("MIME type must be present");
        new EmptyEnvelopeDocument(DOCUMENT_NAME, null, Collections.singletonList(hash));
    }


    @Test
    public void testCreateEmptyDocumentWithoutDataHash_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Data hash list must be present");
        new EmptyEnvelopeDocument(DOCUMENT_NAME, MIME_TYPE_APPLICATION_TXT, null);
    }

    @Test
    public void testGetDataHashList() throws Exception {
        List<DataHash> hashes = Collections.singletonList(hash);
        EnvelopeDocument doc = new EmptyEnvelopeDocument(DOCUMENT_NAME, MIME_TYPE_APPLICATION_TXT, hashes);
        assertEquals(hashes, doc.getDataHashList(Collections.singletonList(hash.getAlgorithm())));
    }

    @Test
    public void testGetDataHashListForNotPresentAlgorithm() throws Exception {
        expectedException.expect(DataHashException.class);
        expectedException.expectMessage("Could not find any pre-generated hashes for requested algorithms!");
        List<DataHash> hashes = Collections.singletonList(Util.hash(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)), HashAlgorithm.SHA2_256));
        EnvelopeDocument doc = new EmptyEnvelopeDocument(DOCUMENT_NAME, MIME_TYPE_APPLICATION_TXT, hashes);
        doc.getDataHashList(Collections.singletonList(HashAlgorithm.RIPEMD_160));
    }
}