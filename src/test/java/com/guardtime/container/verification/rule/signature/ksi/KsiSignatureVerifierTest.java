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

package com.guardtime.container.verification.rule.signature.ksi;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.verification.result.SignatureResult;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.container.verification.rule.signature.SignatureVerifier;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.publication.PublicationData;
import com.guardtime.ksi.unisignature.KSISignature;
import com.guardtime.ksi.unisignature.verifier.VerificationResult;
import com.guardtime.ksi.unisignature.verifier.policies.Policy;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KsiSignatureVerifierTest extends AbstractContainerTest {

    @Test
    public void testCreateWithoutKSI_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("KSI");
        new KsiSignatureVerifier(null, null);
    }

    @Test
    public void testIsSupportedReturnsTrueForKSISignatures() {
        ContainerSignature mockContainerSignature = Mockito.mock(ContainerSignature.class);
        when(mockContainerSignature.getSignature()).thenReturn(Mockito.mock(KSISignature.class));
        SignatureVerifier verifier = new KsiSignatureVerifier(Mockito.mock(KSI.class), Mockito.mock(Policy.class));

        assertTrue(verifier.isSupported(mockContainerSignature));
    }

    @Test
    public void testVerifyUsesPublicationData() throws Exception {
        KSI mockKsi = Mockito.mock(KSI.class);
        Policy policy = Mockito.mock(Policy.class);
        Manifest mockManifest = Mockito.mock(Manifest.class);
        KSISignature mockSignature = Mockito.mock(KSISignature.class);
        PublicationData publicationData = Mockito.mock(PublicationData.class);
        DataHash nullDataHash = new DataHash(HashAlgorithm.SHA2_256, new byte[32]);


        when(mockSignature.getInputHash()).thenReturn(nullDataHash);
        when(mockManifest.getDataHash(HashAlgorithm.SHA2_256)).thenReturn(nullDataHash);
        when(mockKsi.verify(Mockito.any(KSISignature.class), Mockito.any(Policy.class), Mockito.any(DataHash.class), Mockito.any(PublicationData.class))).thenReturn(Mockito.mock(VerificationResult.class));


        SignatureVerifier<KSISignature> verifier = new KsiSignatureVerifier(mockKsi, policy, publicationData);
        verifier.getSignatureVerificationResult(mockSignature, mockManifest);
        verify(mockKsi, times(1)).verify(mockSignature, policy, nullDataHash, publicationData);
    }

    @Test
    public void testVerifyRaisesException() throws Exception {
        expectedException.expect(RuleTerminatingException.class);
        expectedException.expectMessage("Failed to verify KSI signature.");

        KSI mockKsi = Mockito.mock(KSI.class);
        Manifest mockManifest = Mockito.mock(Manifest.class);
        KSISignature mockSignature = Mockito.mock(KSISignature.class);
        DataHash nullDataHash = new DataHash(HashAlgorithm.SHA2_256, new byte[32]);


        when(mockSignature.getInputHash()).thenReturn(nullDataHash);
        when(mockManifest.getDataHash(HashAlgorithm.SHA2_256)).thenReturn(nullDataHash);
        when(mockKsi.verify(Mockito.any(KSISignature.class), Mockito.any(Policy.class), Mockito.any(DataHash.class), Mockito.any(PublicationData.class))).thenThrow(KSIException.class);

        SignatureVerifier<KSISignature> verifier = new KsiSignatureVerifier(mockKsi, Mockito.mock(Policy.class));
        verifier.getSignatureVerificationResult(mockSignature, mockManifest);
    }

    @Test
    public void testVerifyReturnsResult() throws Exception {
        KSI mockKsi = Mockito.mock(KSI.class);
        Policy policy = Mockito.mock(Policy.class);
        Manifest mockManifest = Mockito.mock(Manifest.class);
        KSISignature mockSignature = Mockito.mock(KSISignature.class);
        PublicationData publicationData = Mockito.mock(PublicationData.class);
        DataHash nullDataHash = new DataHash(HashAlgorithm.SHA2_256, new byte[32]);
        VerificationResult mockKsiVerificationResult = Mockito.mock(VerificationResult.class);


        when(mockSignature.getInputHash()).thenReturn(nullDataHash);
        when(mockManifest.getDataHash(HashAlgorithm.SHA2_256)).thenReturn(nullDataHash);
        when(mockKsiVerificationResult.isOk()).thenReturn(true);
        when(mockKsi.verify(Mockito.any(KSISignature.class), Mockito.any(Policy.class), Mockito.any(DataHash.class), Mockito.any(PublicationData.class))).thenReturn(mockKsiVerificationResult);


        SignatureVerifier<KSISignature> verifier = new KsiSignatureVerifier(mockKsi, policy, publicationData);
        SignatureResult result = verifier.getSignatureVerificationResult(mockSignature, mockManifest);

        assertEquals(mockSignature, result.getSignature());
        assertEquals(com.guardtime.container.verification.result.VerificationResult.OK, result.getSimplifiedResult());
        assertEquals(mockKsiVerificationResult, result.getFullResult());

    }

}