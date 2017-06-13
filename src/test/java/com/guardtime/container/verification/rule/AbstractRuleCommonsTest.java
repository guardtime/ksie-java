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

package com.guardtime.container.verification.rule;

import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.rule.state.RuleState;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class AbstractRuleCommonsTest {

    @Test
    public void testVerifySkipsVerificationForIgnoreState() throws Exception {
        TestRule ruleSpy = spy(new TestRule(RuleState.IGNORE));
        assertFalse(ruleSpy.verify(Mockito.mock(ResultHolder.class), ""));
        verify(ruleSpy, never()).verifyRule(Mockito.any(ResultHolder.class), Mockito.any());
    }

    private class TestRule extends AbstractRule {
        TestRule(RuleState state) {
            super(state);
        }

        @Override
        public void verifyRule(ResultHolder holder, Object verifiable) throws RuleTerminatingException {
        }
    }

}