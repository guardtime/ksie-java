package com.guardtime.container.verification.rule.signature.ksi;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.verification.rule.signature.SignatureVerifier;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.publication.PublicationData;
import com.guardtime.ksi.unisignature.KSISignature;
import com.guardtime.ksi.unisignature.verifier.VerificationResult;
import com.guardtime.ksi.unisignature.verifier.policies.Policy;

import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KsiBasedSignatureVerifierTest extends AbstractContainerTest {

    @Test
    public void testCreateWithoutKSI_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("KSI");
        new KsiBasedSignatureVerifier(null, null);
    }

    @Test
    public void testVerifyUsesPublicationData() throws Exception {
        KSI mockKsi = Mockito.mock(KSI.class);
        Manifest mockManifest = Mockito.mock(Manifest.class);
        PublicationData publicationData = Mockito.mock(PublicationData.class);
        Policy policy = Mockito.mock(Policy.class);
        DataHash nullDataHash = new DataHash(HashAlgorithm.SHA2_256, new byte[32]);
        KSISignature mockSignature = Mockito.mock(KSISignature.class);


        when(mockSignature.getInputHash()).thenReturn(nullDataHash);
        when(mockManifest.getDataHash(HashAlgorithm.SHA2_256)).thenReturn(nullDataHash);
        when(mockKsi.verify(Mockito.any(KSISignature.class), Mockito.any(Policy.class), Mockito.any(DataHash.class), Mockito.any(PublicationData.class))).thenReturn(Mockito.mock(VerificationResult.class));


        SignatureVerifier<KSISignature> verifier = new KsiBasedSignatureVerifier(mockKsi, policy, publicationData);
        verifier.getSignatureVerificationResult(mockSignature, mockManifest);
        verify(mockKsi, times(1)).verify(mockSignature, policy, nullDataHash, publicationData);
    }

}