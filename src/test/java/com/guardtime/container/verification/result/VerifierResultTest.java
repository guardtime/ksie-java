package com.guardtime.container.verification.result;

import com.guardtime.container.ContainerFileElement;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.rule.Rule;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class VerifierResultTest {

    @Mock
    private VerificationContext mockVerificationContext;

    @Mock
    private Rule mockRule;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    private void setUpResults(RuleResult ... results){
        List<RuleVerificationResult> genericVerificationResults = new LinkedList<>();
        for(RuleResult result : results) {
            genericVerificationResults.add(new GenericVerificationResult(result, mockRule, new ContainerFileElement() {
            }));
        }
        when(mockVerificationContext.getResults()).thenReturn(genericVerificationResults);
    }

    @Test
    public void testGetVerificationResultReturnsOK() throws Exception {
        setUpResults(RuleResult.OK, RuleResult.OK, RuleResult.OK);
        VerifierResult result = new VerifierResult(mockVerificationContext);
        assertEquals(RuleResult.OK, result.getVerificationResult());
    }

    @Test
    public void testGetVerificationResultReturnsWARN() throws Exception {
        setUpResults(RuleResult.OK, RuleResult.WARN, RuleResult.OK, RuleResult.OK);
        VerifierResult result = new VerifierResult(mockVerificationContext);
        assertEquals(RuleResult.WARN, result.getVerificationResult());
    }

    @Test
    public void testGetVerificationResultReturnsNOK() throws Exception {
        setUpResults(RuleResult.OK, RuleResult.OK, RuleResult.OK, RuleResult.NOK);
        VerifierResult result = new VerifierResult(mockVerificationContext);
        assertEquals(RuleResult.NOK, result.getVerificationResult());
    }
}