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

import com.guardtime.envelope.annotation.EnvelopeAnnotationType;
import com.guardtime.envelope.util.Util;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.tlv.TLVElement;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class TlvSingleAnnotationManifestReferenceTest extends AbstractTlvManifestTest {

    @Test
    public void testCreateSingleAnnotationManifestReference() throws Exception {
        when(mockAnnotation.getAnnotationType()).thenReturn(EnvelopeAnnotationType.FULLY_REMOVABLE);
        TlvSingleAnnotationManifestReference reference = new TlvSingleAnnotationManifestReference(
                mockAnnotation,
                mockSingleAnnotationManifest,
                DEFAULT_HASH_ALGORITHM_PROVIDER
        );
        assertEquals(ANNOTATION_INFO_REFERENCE_TYPE, reference.getElementType());
        assertEquals(EnvelopeAnnotationType.FULLY_REMOVABLE.getContent(), getMimeType(reference));
        assertEquals(MOCK_URI, getUri(reference));
        assertEquals(Util.hash(mockSingleAnnotationManifest.getInputStream(), HashAlgorithm.SHA2_256), getDataHash(reference));
    }

    @Test
    public void testReadSingleAnnotationManifestReference() throws Exception {
        TLVElement element = createReference(ANNOTATION_INFO_REFERENCE_TYPE, MOCK_URI, MIME_TYPE_APPLICATION_TXT, dataHash);
        TlvSingleAnnotationManifestReference reference = new TlvSingleAnnotationManifestReference(element);
        assertEquals(MOCK_URI, reference.getUri());
        assertEquals(MIME_TYPE_APPLICATION_TXT, reference.getMimeType());
        assertEquals(dataHash, reference.getHashList().get(0));
    }

}