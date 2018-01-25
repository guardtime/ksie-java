/*
 * Copyright 2013-2018 Guardtime, Inc.
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

import com.guardtime.envelope.util.Util;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TlvAnnotationsManifestReferenceTest extends AbstractTlvManifestTest {

    @Test
    public void testCreateAnnotationsManifestReference() throws Exception {
        TlvAnnotationsManifestReference reference =
                new TlvAnnotationsManifestReference(mockAnnotationsManifest, DEFAULT_HASH_ALGORITHM_PROVIDER);
        assertEquals(ANNOTATIONS_MANIFEST_REFERENCE_TYPE, reference.getElementType());
        assertEquals(ANNOTATIONS_MANIFEST_TYPE, getMimeType(reference));
        assertEquals(MOCK_URI, getUri(reference));
        assertEquals(Util.hash(mockAnnotationsManifest.getInputStream(), HashAlgorithm.SHA2_256), getDataHash(reference));
    }

    @Test
    public void testReadAnnotationsManifestReference() throws Exception {
        TLVElement element = createReference(ANNOTATIONS_MANIFEST_REFERENCE_TYPE, MOCK_URI, ANNOTATIONS_MANIFEST_TYPE, dataHash);
        TlvAnnotationsManifestReference reference = new TlvAnnotationsManifestReference(element);
        assertEquals(MOCK_URI, reference.getUri());
        assertEquals(ANNOTATIONS_MANIFEST_TYPE, reference.getMimeType());
        assertEquals(dataHash, reference.getHashList().get(0));
    }

}
