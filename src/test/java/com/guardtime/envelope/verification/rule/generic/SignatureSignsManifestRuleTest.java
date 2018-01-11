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

package com.guardtime.envelope.verification.rule.generic;

import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.verification.result.ResultHolder;
import com.guardtime.envelope.verification.result.RuleVerificationResult;
import com.guardtime.envelope.verification.result.VerificationResult;
import com.guardtime.envelope.verification.rule.Rule;
import com.guardtime.envelope.verification.rule.RuleTerminatingException;
import com.guardtime.envelope.verification.rule.state.DefaultRuleStateProvider;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class SignatureSignsManifestRuleTest {

    private Rule rule = new SignatureSignsManifestRule(new DefaultRuleStateProvider());
    private DataHash nullDataHash = new DataHash(HashAlgorithm.SHA2_256, new byte[32]);

    @Mock
    private SignatureContent mockSignatureContent;

    @Mock
    private Manifest mockManifest;

    @Mock
    private EnvelopeSignature mockEnvelopeSignature;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(mockManifest.getDataHash(Mockito.any(HashAlgorithm.class))).thenReturn(nullDataHash);
        when(mockEnvelopeSignature.getSignedDataHash()).thenReturn(nullDataHash);
        when(mockSignatureContent.getManifest()).thenReturn(mockManifest);
        when(mockSignatureContent.getEnvelopeSignature()).thenReturn(mockEnvelopeSignature);
    }

    @Test
    public void testValidSignatureContent() {
        when(mockEnvelopeSignature.getSignedDataHash()).thenReturn(nullDataHash);
        assertRuleResult(VerificationResult.OK);
    }

    @Test
    public void testInvalidSignatureContent() {
        when(mockEnvelopeSignature.getSignedDataHash()).thenReturn(new DataHash(HashAlgorithm.SHA2_384, new byte[48]));
        assertRuleResult(VerificationResult.NOK);
    }

    private void assertRuleResult(VerificationResult result) {
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