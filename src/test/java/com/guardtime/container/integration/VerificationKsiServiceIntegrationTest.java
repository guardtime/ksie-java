package com.guardtime.container.integration;

import com.guardtime.container.packaging.Container;
import com.guardtime.container.verification.ContainerVerifier;
import com.guardtime.container.verification.policy.DefaultVerificationPolicy;
import com.guardtime.container.verification.policy.VerificationPolicy;
import com.guardtime.container.verification.result.ContainerVerifierResult;
import com.guardtime.container.verification.result.SignatureResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.signature.ksi.KsiPolicyBasedSignatureVerifier;
import com.guardtime.ksi.unisignature.verifier.policies.CalendarBasedVerificationPolicy;
import org.junit.Test;

import java.io.FileInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VerificationKsiServiceIntegrationTest extends AbstractCommonKsiServiceIntegrationTest {

    @Test
    public void testContainerWithInvalidSignature_VerificationFails() throws Exception {
        FileInputStream fis = new FileInputStream(loadFile(CONTAINER_WITH_WRONG_SIGNATURE_FILE));
        Container container = packagingFactory.read(fis);
        VerificationPolicy defaultPolicy = new DefaultVerificationPolicy(
                defaultRuleStateProvider,
                new KsiPolicyBasedSignatureVerifier(ksi, new CalendarBasedVerificationPolicy()),
                packagingFactory
        );
        ContainerVerifier verifier = new ContainerVerifier(defaultPolicy);
        ContainerVerifierResult verifierResult = verifier.verify(container);
        SignatureResult signatureResult = verifierResult.getSignatureResult(container.getSignatureContents().get(0));
        assertEquals(VerificationResult.NOK, verifierResult.getVerificationResult());
        assertNotNull(signatureResult);
        assertEquals(VerificationResult.NOK, signatureResult.getSimplifiedResult());
        assertNotNull(signatureResult.getFullResult());
    }
}
