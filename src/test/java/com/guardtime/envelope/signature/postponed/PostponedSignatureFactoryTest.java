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

package com.guardtime.envelope.signature.postponed;

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.envelope.extending.ExtendingPolicy;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.signature.SignatureException;
import com.guardtime.envelope.signature.SignatureFactory;
import com.guardtime.envelope.signature.SignatureFactoryType;
import com.guardtime.envelope.signature.ksi.KsiSignatureFactory;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.unisignature.KSISignature;

import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PostponedSignatureFactoryTest extends AbstractEnvelopeTest {

    public static final DataHash DATA_HASH = new DataHash(HashAlgorithm.SHA2_256, "32323232323232323232323232323232".getBytes());
    private PostponedSignatureFactory limitedFactory = new PostponedSignatureFactory(mock(SignatureFactoryType.class));
    private final KSI mockKsi = mock(KSI.class);
    private SignatureFactory spySignatureFactory = spy(new KsiSignatureFactory(mockKsi));
    private PostponedSignatureFactory fullFactory = new PostponedSignatureFactory(spySignatureFactory);

    @Test
    public void testExtending_ThrowsIllegalStateException() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Not supported if SignatureFactory is not provided to constructor!");
        limitedFactory.extend(mock(EnvelopeSignature.class), mock(ExtendingPolicy.class));
    }

    @Test
    public void testSigning_ThrowsIllegalStateException() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Not supported if SignatureFactory is not provided to constructor!");
        limitedFactory.sign(mock(SignatureContent.class));
    }

    @Test
    public void testExtending() throws Exception {
        doNothing().when(spySignatureFactory).extend(any(EnvelopeSignature.class), any(ExtendingPolicy.class));
        EnvelopeSignature sampleSignature = spySignatureFactory.create(
                DATA_HASH
        );
        fullFactory.extend(sampleSignature, mock(ExtendingPolicy.class));
        verify(spySignatureFactory, times(1)).extend(any(EnvelopeSignature.class), any(ExtendingPolicy.class));
    }

    @Test
    public void testSigning() throws Exception {
        SignatureContent mockSignatureContent = getMockedSignatureContent(DATA_HASH);
        fullFactory.sign(mockSignatureContent);
        verify(spySignatureFactory, times(1)).create(any(DataHash.class));
    }

    @Test
    public void testSignWithWrongSignature_ThrowsSignatureException() throws Exception {
        expectedException.expect(SignatureException.class);
        expectedException.expectMessage("Failed to assign signature to placeholder as it already has a signature!");
        SignatureContent mockSignatureContent = getMockedSignatureContent(DATA_HASH);
        fullFactory.sign(mockSignatureContent);
        fullFactory.sign(mockSignatureContent);
    }

    @Test
    public void testSignWithWrongSignature_ThrowsIllegalArgumentException() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Provided signatures Data hash does not match!");
        SignatureContent mockSignatureContent = getMockedSignatureContent(mock(DataHash.class));
        fullFactory.sign(mockSignatureContent);
    }

    @Test
    public void testCreating() {
        EnvelopeSignature signature = limitedFactory.create(DATA_HASH);
        assertEquals(DATA_HASH, signature.getSignedDataHash());
    }

    @Test
    public void testParsing() throws Exception {
        try (ByteArrayInputStream input = new ByteArrayInputStream(DATA_HASH.getImprint())) {
            EnvelopeSignature signature = limitedFactory.read(input);
            assertEquals(DATA_HASH, signature.getSignedDataHash());
        }
    }

    private SignatureContent getMockedSignatureContent(DataHash dataHash) throws KSIException {
        SignatureContent mockSignatureContent = mock(SignatureContent.class);
        EnvelopeSignature mockEnvelopeSignature = new PostponedSignature(
                DATA_HASH
        );
        when(mockSignatureContent.getEnvelopeSignature()).thenReturn(mockEnvelopeSignature);
        KSISignature mockKsiSignature = mock(KSISignature.class);
        when(mockKsiSignature.getInputHash()).thenReturn(dataHash);
        when(mockKsi.sign(any(DataHash.class))).thenReturn(mockKsiSignature);
        return mockSignatureContent;
    }

}
