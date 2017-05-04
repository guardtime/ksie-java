package com.guardtime.container.verification.result;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ResultHolderTest {

    private ResultHolder holder = new ResultHolder();

    @Test
    public void addResult() throws Exception {
        RuleVerificationResult mockResult = Mockito.mock(RuleVerificationResult.class);
        holder.addResult(mockResult);
        assertEquals(mockResult, holder.getResults().get(0));
    }

    @Test
    public void addResults() throws Exception {
        List<RuleVerificationResult> mockResults = Arrays.asList(Mockito.mock(RuleVerificationResult.class), Mockito.mock(RuleVerificationResult.class), Mockito.mock(RuleVerificationResult.class));
        holder.addResults(mockResults);
        assertTrue(mockResults.containsAll(holder.getResults()));
        assertTrue(holder.getResults().containsAll(mockResults));
    }

}