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
    public void getSignatureResults() throws Exception {
        SignatureResult mockSignatureResult = Mockito.mock(SignatureResult.class);
        String path = "path";
        holder.setSignatureResult(path, mockSignatureResult);
        SignatureResult mockSignatureResult2 = Mockito.mock(SignatureResult.class);
        String path2 = "path2";
        holder.setSignatureResult(path2, mockSignatureResult2);

        assertTrue(holder.getSignatureResults().containsKey(path));
        assertEquals(mockSignatureResult, holder.getSignatureResults().get(path));
        assertTrue(holder.getSignatureResults().containsKey(path2));
        assertEquals(mockSignatureResult2, holder.getSignatureResults().get(path2));
    }

    @Test
    public void getSignatureResult() throws Exception {
        SignatureResult mockSignatureResult = Mockito.mock(SignatureResult.class);
        String path = "path";
        holder.setSignatureResult(path, mockSignatureResult);
        assertEquals(mockSignatureResult, holder.getSignatureResult(path));
    }

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