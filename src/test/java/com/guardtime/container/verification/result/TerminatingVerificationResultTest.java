package com.guardtime.container.verification.result;

import com.guardtime.container.verification.rule.Rule;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class TerminatingVerificationResultTest {
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
        result = new TerminatingVerificationResult(VERIFICATION_RESULT, mockRule, TESTED_ELEMENT);
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
    public void terminatesVerification() throws Exception {
        assertTrue(result.terminatesVerification());
    }

    @Test
    public void getRuleErrorMessageWithExceptionInConstructor() throws Exception {
        String exceptionMessage = "The exception occurred!";
        Exception exception = new Exception(exceptionMessage);
        RuleVerificationResult genericResult = new TerminatingVerificationResult(VERIFICATION_RESULT, mockRule, TESTED_ELEMENT, exception);
        assertNotNull(genericResult.getRuleErrorMessage());
        assertEquals(exceptionMessage, genericResult.getRuleErrorMessage());
    }

}