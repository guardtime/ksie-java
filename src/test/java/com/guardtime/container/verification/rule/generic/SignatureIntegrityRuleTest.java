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

package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.manifest.SignatureReference;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.SignatureResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.signature.SignatureVerifier;
import com.guardtime.container.verification.rule.state.DefaultRuleStateProvider;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class SignatureIntegrityRuleTest {

    private SignatureVerifier mockSignatureVerifier = Mockito.mock(SignatureVerifier.class);
    private Rule rule = new SignatureIntegrityRule(new DefaultRuleStateProvider(), mockSignatureVerifier);

    @Test
    public void testSignatureIsInvalidResultsInNOK() throws Exception {
        SignatureContent mockSignatureContent = setUpMockSignatureContent(Mockito.mock(SignatureResult.class));

        ResultHolder holder = new ResultHolder();
        rule.verify(holder, mockSignatureContent);

        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.NOK, result.getVerificationResult());
    }

    @Test
    public void testSignatureIsValidResultsInOK() throws Exception {
        SignatureResult mockSignatureResult = Mockito.mock(SignatureResult.class);
        SignatureContent mockSignatureContent = setUpMockSignatureContent(mockSignatureResult);
        when(mockSignatureResult.getSimplifiedResult()).thenReturn(VerificationResult.OK);

        ResultHolder holder = new ResultHolder();
        rule.verify(holder, mockSignatureContent);

        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    private SignatureContent setUpMockSignatureContent(SignatureResult mockSignatureResult) throws Exception {
        Manifest mockedManifest = Mockito.mock(Manifest.class);
        SignatureContent mockSignatureContent = Mockito.mock(SignatureContent.class);
        SignatureReference mockSignatureReference = Mockito.mock(SignatureReference.class);
        ContainerSignature mockContainerSignature = Mockito.mock(ContainerSignature.class);

        when(mockSignatureContent.getContainerSignature()).thenReturn(mockContainerSignature);
        when(mockSignatureContent.getManifest()).thenReturn(Pair.of("path", mockedManifest));
        when(mockedManifest.getSignatureReference()).thenReturn(mockSignatureReference);
        when(mockSignatureReference.getUri()).thenReturn("signaturePath.ext");

        when(mockSignatureVerifier.isSupported(mockContainerSignature)).thenReturn(true);
        when(mockSignatureVerifier.getSignatureVerificationResult(Mockito.any(), eq(mockedManifest))).thenReturn(mockSignatureResult);
        return mockSignatureContent;
    }
}