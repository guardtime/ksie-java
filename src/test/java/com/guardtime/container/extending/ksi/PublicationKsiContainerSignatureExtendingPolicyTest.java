package com.guardtime.container.extending.ksi;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.extending.ExtendingPolicy;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.publication.PublicationRecord;
import com.guardtime.ksi.unisignature.KSISignature;

import org.junit.Test;
import org.mockito.Mockito;

public class PublicationKsiContainerSignatureExtendingPolicyTest extends AbstractContainerTest {

    @Test
    public void testPublicationKsiContainerSignatureExtenderWithoutKsi_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("KSI");
        new PublicationKsiContainerSignatureExtendingPolicy(null, Mockito.mock(PublicationRecord.class));
    }

    @Test
    public void testPublicationKsiContainerSignatureExtenderWithoutPublicationRecord_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Publication record");
        new PublicationKsiContainerSignatureExtendingPolicy(Mockito.mock(KSI.class), null);
    }

    @Test
    public void testExtendingDelegatesToKsi() throws Exception {
        KSI mockKsi = Mockito.mock(KSI.class);
        ExtendingPolicy<KSISignature> extendingPolicy = new PublicationKsiContainerSignatureExtendingPolicy(mockKsi, Mockito.mock(PublicationRecord.class));
        extendingPolicy.getExtendedSignature(Mockito.mock(KSISignature.class));
        Mockito.verify(mockKsi, Mockito.times(1)).extend(Mockito.any(KSISignature.class), Mockito.any(PublicationRecord.class));
    }

}