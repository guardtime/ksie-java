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

package com.guardtime.container.manifest.tlv;

import com.guardtime.container.util.Pair;
import com.guardtime.ksi.tlv.TLVElement;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TlvAnnotationDataReferenceTest extends AbstractTlvManifestTest {

    @Test
    public void testCreateAnnotationReference() throws Exception {
        TlvAnnotationDataReference reference = new TlvAnnotationDataReference(Pair.of(MOCK_URI, mockAnnotation), DEFAULT_HASH_ALGORITHM_PROVIDER);
        assertEquals(ANNOTATION_REFERENCE_TYPE, reference.getElementType());
        assertEquals(ANNOTATION_DOMAIN_COM_GUARDTIME, getDomain(reference));
        assertEquals(MOCK_URI, getUri(reference));
        assertEquals(dataHash, getDataHash(reference));
    }

    @Test
    public void testReadAnnotationReference() throws Exception {
        TLVElement element = createAnnotationReferenceElement();
        TlvAnnotationDataReference reference = new TlvAnnotationDataReference(element);
        assertEquals(MOCK_URI, reference.getUri());
        assertEquals(ANNOTATION_DOMAIN_COM_GUARDTIME, reference.getDomain());
        assertEquals(dataHash, reference.getHash());
    }

}