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

package com.guardtime.envelope.manifest.tlv;

import com.guardtime.ksi.tlv.TLVElement;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class TlvDocumentsManifestReferenceTest extends AbstractTlvManifestTest {

    @Test
    public void testCreateDocumentsManifestReference() throws Exception {
        TlvDocumentsManifest documentsManifest = new TlvDocumentsManifest(Collections.singletonList(TEST_DOCUMENT_HELLO_TEXT), DEFAULT_HASH_ALGORITHM_PROVIDER, TEST_FILE_NAME_TEST_TXT);
        TlvDocumentsManifestReference reference = new TlvDocumentsManifestReference(documentsManifest, DEFAULT_HASH_ALGORITHM_PROVIDER);
        assertEquals(DOCUMENTS_MANIFEST_REFERENCE_TYPE, reference.getElementType());
        assertEquals(DOCUMENTS_MANIFEST_TYPE, getMimeType(reference));
        assertEquals(TEST_FILE_NAME_TEST_TXT, getUri(reference));
    }

    @Test
    public void testReadDocumentsManifestReference() throws Exception {
        TLVElement element = createReference(DOCUMENTS_MANIFEST_REFERENCE_TYPE, TEST_FILE_NAME_TEST_TXT, MIME_TYPE_APPLICATION_TXT, dataHash);
        TlvDocumentsManifestReference reference = new TlvDocumentsManifestReference(element);
        assertEquals(TEST_FILE_NAME_TEST_TXT, reference.getUri());
        assertEquals(MIME_TYPE_APPLICATION_TXT, reference.getMimeType());
        assertEquals(dataHash, reference.getHashList().get(0));
    }

}