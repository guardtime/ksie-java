package com.guardtime.container.integration;

import com.guardtime.container.packaging.Container;
import com.guardtime.container.verification.ContainerVerifier;
import com.guardtime.container.verification.policy.DefaultVerificationPolicy;
import com.guardtime.container.verification.policy.VerificationPolicy;
import com.guardtime.container.verification.result.ContainerVerifierResult;
import com.guardtime.container.verification.result.SignatureResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.signature.ksi.KsiSignatureVerifier;
import com.guardtime.ksi.publication.PublicationData;
import com.guardtime.ksi.unisignature.verifier.PolicyVerificationResult;
import com.guardtime.ksi.unisignature.verifier.VerificationErrorCode;
import com.guardtime.ksi.unisignature.verifier.VerificationResultCode;
import com.guardtime.ksi.unisignature.verifier.policies.*;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.List;

import static org.junit.Assert.*;

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

        com.guardtime.ksi.unisignature.verifier.VerificationResult signatureVerificationFullResults = (com.guardtime.ksi.unisignature.verifier.VerificationResult) signatureResult.getFullResult();
        assertNotNull(signatureVerificationFullResults);
        assertEquals(VerificationErrorCode.GEN_1, signatureVerificationFullResults.getErrorCode());
        container.close();

    }
    @Test
    public void testUsingInternalVerification() throws Exception {
        Policy signatureVerificationPolicy = new InternalVerificationPolicy();
        Container container = getContainer(CONTAINER_WITH_WRONG_SIGNATURE_FILE);
        ContainerVerifier verifier = getContainerVerifier(signatureVerificationPolicy);
        ContainerVerifierResult result = verifier.verify(container);
        assertTrue(result.getVerificationResult().equals(VerificationResult.NOK));
        SignatureResult signatureResult = result.getSignatureResult(container.getSignatureContents().get(0));
        checkExecutedSignatureVerificationPolicy(signatureVerificationPolicy, VerificationResultCode.FAIL, VerificationErrorCode.GEN_1, signatureResult);
        container.close();
    }

    @Test
    public void testUsingCalendarBasedVerification() throws Exception{
        Policy signatureVerificationPolicy = new CalendarBasedVerificationPolicy();
        Container container = getContainer(CONTAINER_WITH_CHANGED_SIGNATURE_FILE);
        ContainerVerifier verifier = getContainerVerifier(signatureVerificationPolicy);
        ContainerVerifierResult result = verifier.verify(container);
        assertTrue(result.getVerificationResult().equals(VerificationResult.NOK));
        SignatureResult signatureResult = result.getSignatureResult(container.getSignatureContents().get(0));
        checkExecutedSignatureVerificationPolicy(signatureVerificationPolicy, VerificationResultCode.FAIL, VerificationErrorCode.CAL_02, signatureResult);
        container.close();
    }

    @Test
    public void testUsingKeyBasedVerification() throws Exception{
        Policy signatureVerificationPolicy = new KeyBasedVerificationPolicy();
        Container container = getContainer(CONTAINER_WITH_CHANGED_SIGNATURE_FILE);
        ContainerVerifier verifier = getContainerVerifier(signatureVerificationPolicy);
        ContainerVerifierResult result = verifier.verify(container);
        assertTrue(result.getVerificationResult().equals(VerificationResult.NOK));
        SignatureResult signatureResult = result.getSignatureResult(container.getSignatureContents().get(0));
        checkExecutedSignatureVerificationPolicy(signatureVerificationPolicy, VerificationResultCode.FAIL, VerificationErrorCode.KEY_02, signatureResult);
        container.close();
    }

    @Test
    public void testUsingUserPublicationBasedVerification() throws Exception{
        Policy signatureVerificationPolicy = new UserProvidedPublicationBasedVerificationPolicy();
        Container container = getContainer(CONTAINER_WITH_CHANGED_AND_EXTENDED_SIGNATURE_FILE);
        VerificationPolicy policy = new DefaultVerificationPolicy(
                defaultRuleStateProvider,
                new KsiSignatureVerifier(ksi, signatureVerificationPolicy, new PublicationData("AAAAAA-CX4K4D-6AMFWE-EMMHOH-WZT2ZR-Q5MUMQ-DGYCW5-LV5IID-GA672M-LHP5GW-GUGHQN-DA7CGV")),
                packagingFactory
        );
        ContainerVerifier verifier = new ContainerVerifier(policy);
        ContainerVerifierResult result = verifier.verify(container);
        assertTrue(result.getVerificationResult().equals(VerificationResult.NOK));
        SignatureResult signatureResult = result.getSignatureResult(container.getSignatureContents().get(0));
        checkExecutedSignatureVerificationPolicy(signatureVerificationPolicy, VerificationResultCode.FAIL, VerificationErrorCode.INT_09, signatureResult);
        container.close();
    }

    @Test
    public void testUsingPublicationFileBasedVerification() throws Exception{
        Policy signatureVerificationPolicy = new PublicationsFileBasedVerificationPolicy();
        Container container = getContainer(CONTAINER_WITH_CHANGED_SIGNATURE_FILE);
        ContainerVerifier verifier = getContainerVerifier(signatureVerificationPolicy);
        ContainerVerifierResult result = verifier.verify(container);
        assertTrue(result.getVerificationResult().equals(VerificationResult.NOK));
        SignatureResult signatureResult = result.getSignatureResult(container.getSignatureContents().get(0));
        checkExecutedSignatureVerificationPolicy(signatureVerificationPolicy, VerificationResultCode.FAIL, VerificationErrorCode.PUB_03, signatureResult);
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

    private ContainerVerifier getContainerVerifier(Policy policy) {
        VerificationPolicy containerPolicy = new DefaultVerificationPolicy(
                defaultRuleStateProvider,
                new KsiSignatureVerifier(ksi, policy),
                packagingFactory
        );
        return new ContainerVerifier(containerPolicy);
    }

    private void checkExecutedSignatureVerificationPolicy(Policy expectedPolicy, VerificationResultCode expectedResultCode, VerificationErrorCode expectedErrorCode, SignatureResult result) throws Exception {
        com.guardtime.ksi.unisignature.verifier.VerificationResult signatureVerificationResults = (com.guardtime.ksi.unisignature.verifier.VerificationResult) result.getFullResult();
        List<PolicyVerificationResult> verificationResults = signatureVerificationResults.getPolicyVerificationResults();
        boolean expectedPolicyOk = false;
        for (PolicyVerificationResult policyVerificationResult : verificationResults) {
            if (policyVerificationResult.getPolicy().getName().equals(expectedPolicy.getName())) {
                assertEquals(expectedResultCode, policyVerificationResult.getPolicyStatus());
                assertEquals(expectedErrorCode, policyVerificationResult.getErrorCode());
                expectedPolicyOk = true;
                break;
            }
        }
        if (!expectedPolicyOk) {
            throw new Exception("Policy '" + expectedPolicy.getName() + "' not executed.");
        }
    }
}
