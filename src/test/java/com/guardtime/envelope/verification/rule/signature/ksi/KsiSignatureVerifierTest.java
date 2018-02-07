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

package com.guardtime.envelope.verification.rule.signature.ksi;

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.verification.rule.RuleTerminatingException;
import com.guardtime.envelope.verification.rule.signature.SignatureVerifier;
import com.guardtime.ksi.Verifier;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.unisignature.KSISignature;
import com.guardtime.ksi.unisignature.verifier.policies.ContextAwarePolicy;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class KsiSignatureVerifierTest extends AbstractEnvelopeTest {
    private static Verifier signatureVerifier = new com.guardtime.ksi.SignatureVerifier();

    @Test
    public void testCreateWithoutContext_ThrowsNullPointerException() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Context aware policy");
        new KsiSignatureVerifier(signatureVerifier, null);
    }

    @Test
    public void testCreateWithoutSignatureVerifier_ThrowsNullPointerException() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Signature verifier");
        new KsiSignatureVerifier(null, Mockito.mock(ContextAwarePolicy.class));
    }

    @Test
    public void testIsSupportedReturnsTrueForKSISignatures() {
        EnvelopeSignature mockEnvelopeSignature = Mockito.mock(EnvelopeSignature.class);
        when(mockEnvelopeSignature.getSignature()).thenReturn(Mockito.mock(KSISignature.class));
        SignatureVerifier verifier = new KsiSignatureVerifier(signatureVerifier, Mockito.mock(ContextAwarePolicy.class));

        assertTrue(verifier.isSupported(mockEnvelopeSignature));
    }

    @Test
    public void testVerifyRaisesException() throws Exception {
        expectedException.expect(RuleTerminatingException.class);
        expectedException.expectMessage("Failed to verify KSI signature.");

        Manifest mockManifest = Mockito.mock(Manifest.class);
        KSISignature mockSignature = Mockito.mock(KSISignature.class);
        DataHash nullDataHash = new DataHash(HashAlgorithm.SHA2_256, new byte[32]);

        when(mockSignature.getInputHash()).thenReturn(nullDataHash);
        when(mockManifest.getDataHash(HashAlgorithm.SHA2_256)).thenReturn(nullDataHash);

        ContextAwarePolicy mockPolicy = Mockito.mock(ContextAwarePolicy.class);
        when(mockPolicy.getPolicyContext()).thenThrow(KSIException.class);
        SignatureVerifier<KSISignature> verifier = new KsiSignatureVerifier(signatureVerifier, mockPolicy);
        verifier.getSignatureVerificationResult(mockSignature, mockManifest);
    }

}
