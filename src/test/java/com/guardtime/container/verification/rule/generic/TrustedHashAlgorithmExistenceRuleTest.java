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

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class TrustedHashAlgorithmExistenceRuleTest extends AbstractContainerTest {

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
        when(reference.getHashList()).thenReturn(Arrays.asList(nullDataHash));
        rule.verify(holder, reference);

        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

}