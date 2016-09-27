package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.AbstractContainerTest;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.manifest.SignatureReference;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.ksi.unisignature.KSISignature;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class SignatureExistenceRuleTest extends AbstractContainerTest {

    @Mock
    private SignatureContent mockSignatureContent;
    @Mock
    private Manifest mockManifest;
    @Mock
    private SignatureReference mockSignatureReference;

    @Before
    public void setUpSignatureContent() {
        MockitoAnnotations.initMocks(this);
        when(mockSignatureReference.getUri()).thenReturn("uri");
        when(mockManifest.getSignatureReference()).thenReturn(mockSignatureReference);
        when(mockSignatureContent.getManifest()).thenReturn(Pair.of("", mockManifest));
    }

    @Test
    public void testVerifyWithoutSignature() throws Exception {
        when(mockSignatureContent.getContainerSignature()).thenReturn(null);

        assertRuleResult(VerificationResult.NOK);
    }

    @Test
    public void testVerifyWithSignature() throws Exception {
        ContainerSignature mockSignature = Mockito.mock(ContainerSignature.class);
        when(mockSignatureContent.getContainerSignature()).thenReturn(mockSignature);
        when(mockSignature.getSignature()).thenReturn(Mockito.mock(KSISignature.class));

        assertRuleResult(VerificationResult.OK);
    }

    private void assertRuleResult(VerificationResult result) {
        Rule rule = new SignatureExistenceRule(defaultRuleStateProvider);
        ResultHolder holder = new ResultHolder();
        try {
            rule.verify(holder, mockSignatureContent);
        } catch (RuleTerminatingException e) {
            // Drop it as we don't test this at the moment
        }
        for (RuleVerificationResult verificationResult : holder.getResults()) {
            assertEquals(verificationResult.getVerificationResult(), result);
        }
    }

}