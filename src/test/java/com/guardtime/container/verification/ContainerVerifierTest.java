package com.guardtime.container.verification;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.verification.policy.VerificationPolicy;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.rule.ContainerRule;
import com.guardtime.container.verification.rule.RuleTerminatingException;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ContainerVerifierTest extends AbstractContainerTest {

    @Test
    public void testCreateWithoutVerificationPolicy_ThrowsNullPointerException() throws Exception {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Verification policy");
        new ContainerVerifier(null);
    }

    @Test
    public void testTerminatedTopLevelRuleStopsVerification() throws Exception {
        ContainerRule mockContainerRuleFirst = Mockito.mock(ContainerRule.class);
        ContainerRule mockContainerRuleSecond = Mockito.mock(ContainerRule.class);
        ContainerRule mockContainerRuleThird = Mockito.mock(ContainerRule.class);
        VerificationPolicy mockPolicy = Mockito.mock(VerificationPolicy.class);
        when(mockContainerRuleSecond.verify(Mockito.any(ResultHolder.class), Mockito.any(Container.class))).thenThrow(RuleTerminatingException.class);
        when(mockPolicy.getContainerRules()).thenReturn(Arrays.asList(
                mockContainerRuleFirst,
                mockContainerRuleSecond,
                mockContainerRuleThird
        ));

        ContainerVerifier verifier = new ContainerVerifier(mockPolicy);
        verifier.verify(Mockito.mock(Container.class));
        verify(mockContainerRuleThird, never()).verify(Mockito.any(ResultHolder.class), Mockito.any(Container.class));
    }

}