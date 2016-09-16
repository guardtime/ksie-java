package com.guardtime.container.verification.rule;

import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.manifest.SignatureReference;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.container.verification.rule.generic.SignatureExistenceRule;
import com.guardtime.ksi.unisignature.KSISignature;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class SignatureExistenceRuleTest {

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

        Rule rule = new SignatureExistenceRule(RuleState.FAIL);
        List<RuleVerificationResult> results = rule.verify(mockSignatureContent);
        for (RuleVerificationResult verificationResult : results) {
            assertEquals(verificationResult.getVerificationResult(), VerificationResult.NOK);
        }
    }

    @Test
    public void testVerifyWithSignature() throws Exception {
        ContainerSignature mockSignature = Mockito.mock(ContainerSignature.class);
        when(mockSignatureContent.getContainerSignature()).thenReturn(mockSignature);
        when(mockSignature.getSignature()).thenReturn(Mockito.mock(KSISignature.class));

        Rule rule = new SignatureExistenceRule(RuleState.FAIL);
        List<RuleVerificationResult> results = rule.verify(mockSignatureContent);
        for (RuleVerificationResult verificationResult : results) {
            assertEquals(verificationResult.getVerificationResult(), VerificationResult.OK);
        }
    }

}