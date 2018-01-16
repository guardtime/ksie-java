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

package com.guardtime.envelope.verification.result;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ResultHolderTest {

    private ResultHolder holder = new ResultHolder();

    @Test
    public void addResult() {
        RuleVerificationResult mockResult = mock(RuleVerificationResult.class);
        holder.addResult(mockResult);
        assertEquals(mockResult, holder.getResults().get(0));
    }

    @Test
    public void addResults() {
        List<RuleVerificationResult> mockResults = Arrays.asList(
                mock(RuleVerificationResult.class),
                mock(RuleVerificationResult.class),
                mock(RuleVerificationResult.class)
        );
        holder.addResults(mockResults);
        assertTrue(mockResults.containsAll(holder.getResults()));
        assertTrue(holder.getResults().containsAll(mockResults));
    }

}
