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

import com.guardtime.envelope.annotation.EnvelopeAnnotation;
import com.guardtime.envelope.manifest.FileReference;
import com.guardtime.envelope.manifest.InvalidManifestException;
import com.guardtime.envelope.util.Pair;
import com.guardtime.ksi.tlv.TLVElement;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TlvAnnotationsManifestTest extends AbstractTlvManifestTest {

    @Test
    public void testCreateAnnotationsManifest() throws Exception {
        Map<String, Pair<EnvelopeAnnotation, TlvSingleAnnotationManifest>> singleAnnotationManifests = new HashMap<>();
        singleAnnotationManifests.put(MOCK_URI, Pair.of(mockAnnotation, mockSingleAnnotationManifest));
        TlvAnnotationsManifest annotationsManifest = new TlvAnnotationsManifest(singleAnnotationManifests, DEFAULT_HASH_ALGORITHM_PROVIDER);
        assertArrayEquals(ANNOTATIONS_MANIFEST_MAGIC, annotationsManifest.getMagic());
        assertNotNull(annotationsManifest.getSingleAnnotationManifestReferences());
        assertNotNull(annotationsManifest.getSingleAnnotationManifestReferences().get(0));
    }

    @Test
    public void testReadAnnotationsManifest() throws Exception {
        TLVElement annotationsInfoReference = createReference(ANNOTATION_INFO_REFERENCE_TYPE, MOCK_URI, MIME_TYPE_APPLICATION_TXT, dataHash);
        byte[] bytes = join(ANNOTATIONS_MANIFEST_MAGIC, annotationsInfoReference.getEncoded());

        TlvAnnotationsManifest annotationsManifest = new TlvAnnotationsManifest(new ByteArrayInputStream(bytes));
        assertArrayEquals(ANNOTATIONS_MANIFEST_MAGIC, annotationsManifest.getMagic());
        assertNotNull(annotationsManifest.getSingleAnnotationManifestReferences());
        assertEquals(1, annotationsManifest.getSingleAnnotationManifestReferences().size());
        FileReference annotationsReference = annotationsManifest.getSingleAnnotationManifestReferences().get(0);
        assertEquals(MOCK_URI, annotationsReference.getUri());
        assertEquals(MIME_TYPE_APPLICATION_TXT, annotationsReference.getMimeType());
        assertEquals(dataHash, annotationsReference.getHashList().get(0));
    }

    @Test
    public void testReadAnnotationsManifestUsingInvalidMagicBytes_ThrowsInvalidManifestException() throws Exception {
        expectedException.expect(InvalidManifestException.class);
        expectedException.expectMessage("Invalid magic for manifest type");
        new TlvAnnotationsManifest(new ByteArrayInputStream(DOCUMENTS_MANIFEST_MAGIC));
    }

    @Test
    public void testReadAnnotationsManifestWithoutAnnotationReferences() throws Exception {
        TlvAnnotationsManifest annotationsManifest = new TlvAnnotationsManifest(new ByteArrayInputStream(ANNOTATIONS_MANIFEST_MAGIC));
        assertArrayEquals(ANNOTATIONS_MANIFEST_MAGIC, annotationsManifest.getMagic());
        assertNotNull(annotationsManifest.getSingleAnnotationManifestReferences());
        assertTrue(annotationsManifest.getSingleAnnotationManifestReferences().isEmpty());
    }

}