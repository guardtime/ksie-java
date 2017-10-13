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

import com.guardtime.envelope.AbstractEnvelopeTest;
import com.guardtime.envelope.manifest.FileReference;
import com.guardtime.envelope.verification.result.ResultHolder;
import com.guardtime.envelope.verification.result.RuleVerificationResult;
import com.guardtime.envelope.verification.result.VerificationResult;
import com.guardtime.envelope.verification.rule.Rule;
import com.guardtime.envelope.verification.rule.RuleTerminatingException;
import com.guardtime.envelope.verification.rule.state.RuleState;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class TrustedHashAlgorithmExistenceRuleTest extends AbstractEnvelopeTest {

    private Rule rule = new TrustedHashAlgorithmExistenceRule(RuleState.FAIL, "RandomName");

    @Test
    public void testNoHashes() throws Exception {
        expectedException.expect(RuleTerminatingException.class);
        expectedException.expectMessage("No hashes with trusted hash algorithm found.");

        FileReference reference = Mockito.mock(FileReference.class);
        when(reference.getHashList()).thenReturn(new ArrayList<DataHash>());
        rule.verify(new ResultHolder(), reference);
    }

    @Test
    public void testValidHashExistsResultsInOk() throws Exception {
        DataHash nullDataHash = new DataHash(HashAlgorithm.SHA2_256, new byte[32]);
        ResultHolder holder = new ResultHolder();
        FileReference reference = Mockito.mock(FileReference.class);
        when(reference.getHashList()).thenReturn(Collections.singletonList(nullDataHash));
        rule.verify(holder, reference);

        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

}