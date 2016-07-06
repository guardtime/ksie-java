package com.guardtime.container.extending.ksi;

import com.guardtime.container.AbstractContainerTest;
import org.junit.Test;

public class KsiContainerSignatureExtendingPolicyTest extends AbstractContainerTest {

    @Test
    public void testCreatingKsiContainerSignatureExtenderWithoutKsi_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("KSI");
        new KsiContainerSignatureExtendingPolicy(null);
    }

}