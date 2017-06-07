package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.container.verification.rule.state.RuleState;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class UnimplementedHashAlgorithmExistenceRuleTest extends AbstractContainerTest {

    private Rule<FileReference> rule = new UnimplementedHashAlgorithmExistenceRule(RuleState.FAIL, "RuleName");

    @Test
    public void testVerifyNotImplemented_ThrowsRuleTerminatingException() throws Exception {
        expectedException.expect(RuleTerminatingException.class);
        expectedException.expectMessage("Found a hash with not implemented hash algorithm.");
        FileReference mockFileReference = Mockito.mock(FileReference.class);
        DataHash nullDataHash = new DataHash(HashAlgorithm.SHA3_256, new byte[32]);
        when(mockFileReference.getHashList()).thenReturn(Collections.singletonList(nullDataHash));
        rule.verify(new ResultHolder(), mockFileReference);
    }

    @Test
    public void testVerifyAllImplementedResultsInOK() throws Exception {
        FileReference mockFileReference = Mockito.mock(FileReference.class);
        DataHash nullDataHash = new DataHash(HashAlgorithm.SHA2_256, new byte[32]);
        when(mockFileReference.getHashList()).thenReturn(Collections.singletonList(nullDataHash));
        ResultHolder holder = new ResultHolder();
        rule.verify(holder, mockFileReference);
        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

}