package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.MultiHashElement;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.container.verification.rule.state.RuleState;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class MultiHashElementIntegrityRuleTest extends AbstractContainerTest {

    private MultiHashElementIntegrityRule rule = new MultiHashElementIntegrityRule(RuleState.FAIL, "RandomName");
    private DataHash validHash;

    @Before
    public void setUp() throws Exception {
        validHash = new DataHash(HashAlgorithm.SHA2_256, new byte[32]);
    }

    @Test
    public void testMultiHashElementWithValidHashes() throws Exception {
        MultiHashElement element = Mockito.mock(MultiHashElement.class);
        when(element.getDataHash(Mockito.any(HashAlgorithm.class))).thenReturn(validHash);
        FileReference reference = Mockito.mock(FileReference.class);
        when(reference.getHashList()).thenReturn(Arrays.asList(validHash, validHash, validHash));

        ResultHolder holder = new ResultHolder();
        rule.verify(holder, Pair.of(element, reference));
        for (RuleVerificationResult result : holder.getResults()) {
            assertEquals(VerificationResult.OK, result.getVerificationResult());
        }
    }

    @Test
    public void testMultiHashElementWithInvalidHash() throws Exception {
        expectedException.expect(RuleTerminatingException.class);
        expectedException.expectMessage("Hash mismatch found.");

        MultiHashElement element = Mockito.mock(MultiHashElement.class);
        when(element.getDataHash(Mockito.any(HashAlgorithm.class))).thenReturn(Mockito.mock(DataHash.class));
        ResultHolder holder = new ResultHolder();
        FileReference reference = Mockito.mock(FileReference.class);
        when(reference.getHashList()).thenReturn(Arrays.asList(validHash, validHash, validHash));
        rule.verify(holder, Pair.of(element, reference));
    }

    @Test
    public void testMultiHashElementWithNotImplementedHashAlgorithm() throws Exception {
        expectedException.expect(RuleTerminatingException.class);
        expectedException.expectMessage("Found a hash with not implemented hash algorithm");

        MultiHashElement element = Mockito.mock(MultiHashElement.class);
        ResultHolder holder = new ResultHolder();
        FileReference reference = Mockito.mock(FileReference.class);
        when(reference.getHashList()).thenReturn(Arrays.asList(new DataHash(HashAlgorithm.SHA3_256, new byte[32])));
        rule.verify(holder, Pair.of(element, reference));
    }

    @Test
    public void testMultiHashElementWithNotTrustedHashAlgorithm() throws Exception {
        expectedException.expect(RuleTerminatingException.class);
        expectedException.expectMessage("No hashes with trusted hash algorithm found.");

        MultiHashElement element = Mockito.mock(MultiHashElement.class);
        ResultHolder holder = new ResultHolder();
        FileReference reference = Mockito.mock(FileReference.class);
        when(reference.getHashList()).thenReturn(Arrays.asList(new DataHash(HashAlgorithm.SHA1, new byte[20])));
        rule.verify(holder, Pair.of(element, reference));
    }

    @Test
    public void testNoHashes() throws Exception {
        expectedException.expect(RuleTerminatingException.class);
        expectedException.expectMessage("No hashes with trusted hash algorithm found.");

        MultiHashElement element = Mockito.mock(MultiHashElement.class);
        when(element.getDataHash(Mockito.any(HashAlgorithm.class))).thenReturn(Mockito.mock(DataHash.class));
        ResultHolder holder = new ResultHolder();
        FileReference reference = Mockito.mock(FileReference.class);
        when(reference.getHashList()).thenReturn(new ArrayList<DataHash>());
        rule.verify(holder, Pair.of(element, reference));
    }

}