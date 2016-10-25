package com.guardtime.container.extending.ksi;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.extending.ExtendingPolicy;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.unisignature.KSISignature;

import org.junit.Test;
import org.mockito.Mockito;

public class KsiContainerSignatureExtendingPolicyTest extends AbstractContainerTest {

    @Test
    public void testCreatingKsiContainerSignatureExtenderWithoutKsi_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("KSI");
        new KsiContainerSignatureExtendingPolicy(null);
    }

    @Test
    public void testExtendingDelegatesToKsi() throws Exception {
        KSI mockKsi = Mockito.mock(KSI.class);
        ExtendingPolicy<KSISignature> extendingPolicy = new KsiContainerSignatureExtendingPolicy(mockKsi);
        extendingPolicy.getExtendedSignature(Mockito.mock(KSISignature.class));
        Mockito.verify(mockKsi, Mockito.times(1)).extend(Mockito.any(KSISignature.class));
    }

}