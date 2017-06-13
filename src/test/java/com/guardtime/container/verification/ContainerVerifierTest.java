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

package com.guardtime.container.verification;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.verification.policy.VerificationPolicy;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.rule.Rule;
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
        Rule mockContainerRuleFirst = Mockito.mock(Rule.class);
        Rule mockContainerRuleSecond = Mockito.mock(Rule.class);
        Rule mockContainerRuleThird = Mockito.mock(Rule.class);
        VerificationPolicy mockPolicy = Mockito.mock(VerificationPolicy.class);
        when(mockContainerRuleSecond.verify(Mockito.any(ResultHolder.class), Mockito.any(Container.class))).thenThrow(RuleTerminatingException.class);
        when(mockPolicy.getSignatureContentRules()).thenReturn(Arrays.<Rule<SignatureContent>>asList(
                mockContainerRuleFirst,
                mockContainerRuleSecond,
                mockContainerRuleThird
        ));

        ContainerVerifier verifier = new ContainerVerifier(mockPolicy);
        verifier.verify(Mockito.mock(Container.class));
        verify(mockContainerRuleThird, never()).verify(Mockito.any(ResultHolder.class), Mockito.any(Container.class));
    }

}