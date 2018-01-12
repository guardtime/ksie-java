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

package com.guardtime.envelope.extending;

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.manifest.SignatureReference;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.signature.SignatureException;
import com.guardtime.envelope.signature.SignatureFactory;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class EnvelopeSignatureExtenderTest extends AbstractEnvelopeTest {
    private SignatureFactory mockSignatureFactory;
    private EnvelopeSignatureExtender extender;
    private EnvelopeSignature mockSignature;

    @Before
    public void setUp() {
        mockSignatureFactory = mock(SignatureFactory.class);
        mockSignature = mock(EnvelopeSignature.class);
        extender = new EnvelopeSignatureExtender(mockSignatureFactory, mock(ExtendingPolicy.class));
    }

    private Envelope makeMockEnvelope() {
        Envelope mockEnvelope = mock(Envelope.class);
        SignatureContent mockSignatureContent = mock(SignatureContent.class);
        Manifest mockManifest = mock(Manifest.class);
        doReturn(mockSignature).when(mockSignatureContent).getEnvelopeSignature();
        doReturn(Collections.singletonList(mockSignatureContent)).when(mockEnvelope).getSignatureContents();
        doReturn(mock(SignatureReference.class)).when(mockManifest).getSignatureReference();
        doReturn(mockManifest).when(mockSignatureContent).getManifest();
        return mockEnvelope;
    }

    @Test
    public void testCreateWithoutSignatureFactory_ThrowsNullPointerException() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Signature factory");
        new EnvelopeSignatureExtender(null, mock(ExtendingPolicy.class));
    }

    @Test
    public void testCreateWithoutExtendingPolicy_ThrowsNullPointerException() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Extending policy");
        new EnvelopeSignatureExtender(mock(SignatureFactory.class), null);
    }

    @Test
    public void testExtendingSuccess() throws Exception {
        doReturn(true).when(mockSignature).isExtended();
        assertTrue(extender.extend(makeMockEnvelope()).getExtendedSignatureContents().get(0).isExtended());
    }

    @Test
    public void testExtendingFails() throws Exception {
        doThrow(SignatureException.class)
                .when(mockSignatureFactory)
                .extend(Mockito.any(EnvelopeSignature.class), Mockito.any(ExtendingPolicy.class));
        assertFalse(extender.extend(makeMockEnvelope()).getExtendedSignatureContents().get(0).isExtended());
    }

    @Test
    public void testExtendingIsNotDone() throws Exception {
        doReturn(false).when(mockSignature).isExtended();
        assertFalse(extender.extend(makeMockEnvelope()).getExtendedSignatureContents().get(0).isExtended());
    }

}