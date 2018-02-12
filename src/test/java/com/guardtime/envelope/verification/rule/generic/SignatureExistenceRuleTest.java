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

package com.guardtime.envelope.verification.rule.generic;

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.manifest.SignatureReference;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.verification.result.ResultHolder;
import com.guardtime.envelope.verification.result.RuleVerificationResult;
import com.guardtime.envelope.verification.result.VerificationResult;
import com.guardtime.envelope.verification.rule.Rule;
import com.guardtime.envelope.verification.rule.RuleTerminatingException;
import com.guardtime.ksi.unisignature.KSISignature;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class SignatureExistenceRuleTest extends AbstractEnvelopeTest {

    @Mock
    private SignatureContent mockSignatureContent;
    @Mock
    private Manifest mockManifest;
    @Mock
    private SignatureReference mockSignatureReference;

    @Before
    public void setUpSignatureContent() {
        MockitoAnnotations.initMocks(this);
        when(mockSignatureReference.getUri()).thenReturn("uri");
        when(mockManifest.getSignatureReference()).thenReturn(mockSignatureReference);
        when(mockSignatureContent.getManifest()).thenReturn(mockManifest);
    }

    @Test
    public void testVerifyWithoutSignature() {
        when(mockSignatureContent.getEnvelopeSignature()).thenReturn(null);

        assertRuleResult(VerificationResult.NOK);
    }

    @Test
    public void testVerifyWithSignature() {
        EnvelopeSignature mockSignature = Mockito.mock(EnvelopeSignature.class);
        when(mockSignatureContent.getEnvelopeSignature()).thenReturn(mockSignature);
        when(mockSignature.getSignature()).thenReturn(Mockito.mock(KSISignature.class));

        assertRuleResult(VerificationResult.OK);
    }

    private void assertRuleResult(VerificationResult result) {
        Rule rule = new SignatureExistenceRule(defaultRuleStateProvider);
        ResultHolder holder = new ResultHolder();
        try {
            rule.verify(holder, mockSignatureContent);
        } catch (RuleTerminatingException e) {
            // Drop it as we don't test this at the moment
        }
        for (RuleVerificationResult verificationResult : holder.getResults()) {
            assertEquals(verificationResult.getVerificationResult(), result);
        }
    }

}
