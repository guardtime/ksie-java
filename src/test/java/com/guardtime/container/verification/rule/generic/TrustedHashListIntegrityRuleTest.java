package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.MultiHashElement;
import com.guardtime.container.util.Pair;
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

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class TrustedHashListIntegrityRuleTest extends AbstractContainerTest {

    private Rule rule = new TrustedHashListIntegrityRule(RuleState.FAIL, "name");

    @Test
    public void testDataHashesDoNotMatch_ThrowsRuleTerminatingException() throws Exception {
        expectedException.expect(RuleTerminatingException.class);
        expectedException.expectMessage("Hash mismatch found.");
        FileReference mockFileReference = Mockito.mock(FileReference.class);
        DataHash nullDataHash = new DataHash(HashAlgorithm.SHA2_256, new byte[32]);
        when(mockFileReference.getHashList()).thenReturn(Arrays.asList(nullDataHash));
        MultiHashElement mockMultiHashElement = Mockito.mock(MultiHashElement.class);
        when(mockMultiHashElement.getDataHash(Mockito.any(HashAlgorithm.class))).thenReturn(Mockito.mock(DataHash.class));
        rule.verify(new ResultHolder(), Pair.of(mockMultiHashElement, mockFileReference));
    }

    @Test
    public void testDataHashesDoMatchResultsInOk() throws Exception {
        FileReference mockFileReference = Mockito.mock(FileReference.class);
        HashAlgorithm hashAlgorithm = HashAlgorithm.SHA2_256;
        DataHash nullDataHash = new DataHash(hashAlgorithm, new byte[32]);
        when(mockFileReference.getHashList()).thenReturn(Arrays.asList(nullDataHash));
        MultiHashElement mockMultiHashElement = Mockito.mock(MultiHashElement.class);
        when(mockMultiHashElement.getDataHash(hashAlgorithm)).thenReturn(nullDataHash);
        ResultHolder holder = new ResultHolder();
        rule.verify(holder, Pair.of(mockMultiHashElement, mockFileReference));
        RuleVerificationResult result = holder.getResults().get(0);
        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

}