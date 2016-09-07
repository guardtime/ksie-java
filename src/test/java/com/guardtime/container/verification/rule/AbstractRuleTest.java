package com.guardtime.container.verification.rule;

import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.MultiHashElement;
import com.guardtime.container.util.DataHashException;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.hashing.InvalidHashFormatException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class AbstractRuleTest {

    private DataHash dataHash2256;
    private DataHash dataHash2512;
    private DataHash dataHashSha1;
    private DataHash dataHash3512;
    private DataHash dataHashSm3;

    @Mock
    private MultiHashElement mockMultiHashElement;

    @Mock
    private FileReference mockFileReference;

    private AbstractRule rule = new AbstractRule<Object>(RuleState.FAIL) {
        @Override
        protected List<RuleVerificationResult> verifyRule(Object verifiable) {
            return null;
        }
    };

    @Before
    public void setUp() throws IOException, DataHashException, InvalidHashFormatException {
        MockitoAnnotations.initMocks(this);
        when(mockFileReference.getUri()).thenReturn("uri");
        dataHash2256 = new DataHash(HashAlgorithm.SHA2_256, new byte[32]);
        dataHash2512 = new DataHash(HashAlgorithm.SHA2_512, new byte[64]);
        dataHashSha1 = new DataHash(HashAlgorithm.SHA1, new byte[20]);
        dataHash3512 = new DataHash(HashAlgorithm.SHA3_512, new byte[64]);
        dataHashSm3 = new DataHash(HashAlgorithm.SM3, new byte[32]);
        when(mockMultiHashElement.getDataHash(HashAlgorithm.SHA2_256)).thenReturn(dataHash2256);
        when(mockMultiHashElement.getDataHash(HashAlgorithm.SHA2_512)).thenReturn(dataHash2512);
        when(mockMultiHashElement.getDataHash(HashAlgorithm.SHA1)).thenReturn(dataHashSha1);
        when(mockMultiHashElement.getDataHash(HashAlgorithm.SHA3_512)).thenReturn(dataHash3512);
        when(mockMultiHashElement.getDataHash(HashAlgorithm.SM3)).thenReturn(dataHashSm3);
    }

    @Test
    public void testNormalHashes() throws Exception {
        when(mockFileReference.getHashList()).thenReturn(Arrays.asList(dataHash2256, dataHash2512));
        testFileReferenceHashListVerification(mockMultiHashElement, mockFileReference, VerificationResult.OK);
    }

    @Test
    public void testNotTrustedHashes() throws Exception {
        when(mockFileReference.getHashList()).thenReturn(Arrays.asList(dataHashSha1));
        testFileReferenceHashListVerification(mockMultiHashElement, mockFileReference, VerificationResult.OK);
    }

    @Test
    public void testNotImplementedHashes() throws Exception {
        when(mockFileReference.getHashList()).thenReturn(Arrays.asList(dataHash3512, dataHashSm3));
        testFileReferenceHashListVerification(mockMultiHashElement, mockFileReference, VerificationResult.NOK);
    }

    private void testFileReferenceHashListVerification(MultiHashElement mockMultiHashElement, FileReference mockFileReference, VerificationResult expected) {
        List<RuleVerificationResult> results = rule.getFileReferenceHashListVerificationResult(mockMultiHashElement, mockFileReference);
        for (RuleVerificationResult verificationResult : results) {
            assertEquals(expected, verificationResult.getVerificationResult());
        }
    }
}