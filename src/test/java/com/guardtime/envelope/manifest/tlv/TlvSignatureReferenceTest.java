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

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TlvSignatureReferenceTest extends AbstractTlvManifestTest {

    @Test
    public void testCreateSignatureReference() throws Exception {
        TlvSignatureReference reference = new TlvSignatureReference(SIGNATURE_URI, SIGNATURE_MIME_TYPE);
        assertEquals(SIGNATURE_REFERENCE_TYPE, reference.getElementType());
        assertEquals(SIGNATURE_URI, getUri(reference));
        assertEquals(SIGNATURE_MIME_TYPE, getMimeType(reference));
    }

    @Test
    public void testReadSignatureReference() throws Exception {
        TLVElement element = createReference(SIGNATURE_REFERENCE_TYPE, SIGNATURE_URI, SIGNATURE_MIME_TYPE, (List) null);
        TlvSignatureReference signatureReference = new TlvSignatureReference(element);
        assertEquals(SIGNATURE_URI, signatureReference.getUri());
        assertEquals(SIGNATURE_MIME_TYPE, signatureReference.getType());
    }

}
