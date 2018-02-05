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

package com.guardtime.envelope.signature.postponed;

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.signature.SignatureException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PostponedSignatureTest extends AbstractEnvelopeTest {

    private static final DataHash DATA_HASH = new DataHash(HashAlgorithm.SHA2_256, new byte[HashAlgorithm.SHA2_256.getLength()]);

    @Test
    public void testCreateWithNull_ThrowsNullPointerException() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Data hash must be present");
        new PostponedSignature<>(null);
    }

    @Test
    public void testSignOnce() throws SignatureException {
        PostponedSignature signature = new PostponedSignature(DATA_HASH);
        EnvelopeSignature mockSignature = mock(EnvelopeSignature.class);
        when(mockSignature.getSignedDataHash()).thenReturn(DATA_HASH);
        signature.sign(mockSignature);
    }

    @Test
    public void testSignMultiple() throws SignatureException {
        PostponedSignature signature = new PostponedSignature(DATA_HASH);
        EnvelopeSignature mockSignature = mock(EnvelopeSignature.class);
        when(mockSignature.getSignedDataHash()).thenReturn(DATA_HASH);
        signature.sign(mockSignature);

        expectedException.expect(SignatureException.class);
        expectedException.expectMessage("Failed to assign signature to placeholder as it already has a signature!");
        signature.sign(mockSignature);
    }

    @Test
    public void testSignWithRandomSignature_ThrowsIllegalArgumentException() throws SignatureException {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Provided signatures Data hash does not match!");
        PostponedSignature signature = new PostponedSignature(DATA_HASH);
        EnvelopeSignature mockSignature = mock(EnvelopeSignature.class);
        when(mockSignature.getSignedDataHash()).thenReturn(
                new DataHash(HashAlgorithm.SHA2_512, new byte[HashAlgorithm.SHA2_512.getLength()])
        );
        signature.sign(mockSignature);
    }
}
