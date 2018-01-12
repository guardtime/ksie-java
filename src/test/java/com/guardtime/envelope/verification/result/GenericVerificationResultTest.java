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

import com.guardtime.envelope.verification.rule.Rule;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public class GenericVerificationResultTest {
    private static final VerificationResult VERIFICATION_RESULT = VerificationResult.OK;
    private static final String RULE_NAME = "RandomRule";
    private static final String RULE_ERROR_MESSAGE = "Error message of the day";
    private static final String TESTED_ELEMENT = "META-INF/annotation2.dat";
    private RuleVerificationResult result;
    @Mock
    private Rule mockRule;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(mockRule.getName()).thenReturn(RULE_NAME);
        when(mockRule.getErrorMessage()).thenReturn(RULE_ERROR_MESSAGE);
        result = new GenericVerificationResult(
                VERIFICATION_RESULT,
                mockRule.getName(),
                mockRule.getErrorMessage(),
                TESTED_ELEMENT
        );
    }

    @Test
    public void getVerificationResult() throws Exception {
        assertNotNull(result.getVerificationResult());
        assertEquals(VERIFICATION_RESULT, result.getVerificationResult());
    }

    @Test
    public void getRuleName() throws Exception {
        assertNotNull(result.getRuleName());
        assertEquals(RULE_NAME, result.getRuleName());
    }

    @Test
    public void getRuleErrorMessage() throws Exception {
        assertNotNull(result.getRuleErrorMessage());
        assertEquals(RULE_ERROR_MESSAGE, result.getRuleErrorMessage());
    }

    @Test
    public void getTestedElementPath() throws Exception {
        assertNotNull(result.getTestedElementPath());
        assertEquals(TESTED_ELEMENT, result.getTestedElementPath());
    }

    @Test
    public void getRuleErrorMessageWithExceptionInConstructor() throws Exception {
        String exceptionMessage = "The exception occurred!";
        Exception exception = new Exception(exceptionMessage);
        RuleVerificationResult genericResult = new GenericVerificationResult(
                VERIFICATION_RESULT,
                mockRule.getName(),
                mockRule.getErrorMessage(),
                TESTED_ELEMENT,
                exception
        );
        assertNotNull(genericResult.getRuleErrorMessage());
        assertEquals(exceptionMessage, genericResult.getRuleErrorMessage());
    }

}