package com.guardtime.container.verification;

import com.guardtime.container.AbstractContainerTest;
import org.junit.Test;

public class ContainerVerifierTest extends AbstractContainerTest{

    @Test
    public void testCreateWithoutVerificationPolicy_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Verification policy");
        new ContainerVerifier(null);
    }
}