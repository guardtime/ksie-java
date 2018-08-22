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

package com.guardtime.envelope.verification;

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.verification.policy.VerificationPolicy;
import com.guardtime.envelope.verification.result.ResultHolder;
import com.guardtime.envelope.verification.rule.Rule;
import com.guardtime.envelope.verification.rule.RuleTerminatingException;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EnvelopeVerifierTest extends AbstractEnvelopeTest {

    @Test
    public void testCreateWithoutVerificationPolicy_ThrowsNullPointerException() {
        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Verification policy");
        new EnvelopeVerifier(null);
    }

    @Test
    public void testTerminatedTopLevelRuleStopsVerification() throws Exception {
        Rule mockEnvelopeRuleFirst = mock(Rule.class);
        Rule mockEnvelopeRuleSecond = mock(Rule.class);
        Rule mockEnvelopeRuleThird = mock(Rule.class);
        VerificationPolicy mockPolicy = mock(VerificationPolicy.class);
        when(mockEnvelopeRuleSecond.verify(Mockito.any(ResultHolder.class), Mockito.any(Envelope.class)))
                .thenThrow(RuleTerminatingException.class);
        when(mockPolicy.getSignatureContentRules()).thenReturn(Arrays.<Rule<SignatureContent>>asList(
                mockEnvelopeRuleFirst,
                mockEnvelopeRuleSecond,
                mockEnvelopeRuleThird
        ));

        EnvelopeVerifier verifier = new EnvelopeVerifier(mockPolicy);
        Envelope mockEnvelope = mock(Envelope.class);
        when(mockEnvelope.getSignatureContents()).thenReturn(Collections.singletonList(mock(SignatureContent.class)));
        verifier.verify(mockEnvelope);
        verify(mockEnvelopeRuleThird, never()).verify(Mockito.any(ResultHolder.class), Mockito.any(Envelope.class));
    }

}
