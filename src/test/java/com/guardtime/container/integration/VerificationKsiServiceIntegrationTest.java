package com.guardtime.container.integration;

import com.guardtime.container.packaging.Container;
import com.guardtime.container.verification.ContainerVerifier;
import com.guardtime.container.verification.policy.DefaultVerificationPolicy;
import com.guardtime.container.verification.policy.VerificationPolicy;
import com.guardtime.container.verification.result.ContainerVerifierResult;
import com.guardtime.container.verification.result.SignatureResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.signature.ksi.KsiSignatureVerifier;
import com.guardtime.ksi.unisignature.verifier.policies.CalendarBasedVerificationPolicy;

import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VerificationKsiServiceIntegrationTest extends AbstractCommonKsiServiceIntegrationTest {

    private ContainerVerifier verifier;

    @Before
    public void setUpVerifier() {
        VerificationPolicy defaultPolicy = new DefaultVerificationPolicy(
                defaultRuleStateProvider,
                new KsiSignatureVerifier(ksi, new CalendarBasedVerificationPolicy()),
                packagingFactory
        );
        this.verifier = new ContainerVerifier(defaultPolicy);
    }

    private Container getContainer(String filePath) throws Exception {
        FileInputStream fis = new FileInputStream(loadFile(filePath));
        return packagingFactory.read(fis);
    }

    @Test
    public void testContainerWithInvalidSignature_VerificationFails() throws Exception {
        Container container = getContainer(CONTAINER_WITH_WRONG_SIGNATURE_FILE);
        ContainerVerifierResult verifierResult = verifier.verify(container);

        SignatureResult signatureResult = verifierResult.getSignatureResult(container.getSignatureContents().get(0));
        assertEquals(VerificationResult.NOK, verifierResult.getVerificationResult());
        assertNotNull(signatureResult);
        assertEquals(VerificationResult.NOK, signatureResult.getSimplifiedResult());
        assertNotNull(signatureResult.getFullResult());
        container.close();
    }

    @Test
    public void testContainerWithValidSignature_VerificationSucceeds() throws Exception {
        Container container = getContainer(CONTAINER_WITH_ONE_DOCUMENT);
        ContainerVerifierResult verifierResult = verifier.verify(container);

        SignatureResult signatureResult = verifierResult.getSignatureResult(container.getSignatureContents().get(0));
        assertEquals(VerificationResult.OK, verifierResult.getVerificationResult());
        assertNotNull(signatureResult);
        assertEquals(VerificationResult.OK, signatureResult.getSimplifiedResult());
        assertNotNull(signatureResult.getFullResult());
        container.close();
    }
}
