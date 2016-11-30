package com.guardtime.container.integration;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.annotation.StringContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.EmptyContainerDocument;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.ContainerVerifier;
import com.guardtime.container.verification.policy.DefaultVerificationPolicy;
import com.guardtime.container.verification.policy.VerificationPolicy;
import com.guardtime.container.verification.result.ContainerVerifierResult;
import com.guardtime.container.verification.result.SignatureResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.signature.ksi.KsiSignatureVerifier;
import com.guardtime.ksi.hashing.DataHasher;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.publication.PublicationData;
import com.guardtime.ksi.unisignature.verifier.PolicyVerificationResult;
import com.guardtime.ksi.unisignature.verifier.VerificationErrorCode;
import com.guardtime.ksi.unisignature.verifier.VerificationResultCode;
import com.guardtime.ksi.unisignature.verifier.policies.CalendarBasedVerificationPolicy;
import com.guardtime.ksi.unisignature.verifier.policies.InternalVerificationPolicy;
import com.guardtime.ksi.unisignature.verifier.policies.KeyBasedVerificationPolicy;
import com.guardtime.ksi.unisignature.verifier.policies.Policy;
import com.guardtime.ksi.unisignature.verifier.policies.PublicationsFileBasedVerificationPolicy;
import com.guardtime.ksi.unisignature.verifier.policies.UserProvidedPublicationBasedVerificationPolicy;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void testContainerWithInvalidSignature_VerificationFails() throws Exception {
        try (Container container = getContainer(CONTAINER_WITH_WRONG_SIGNATURE_FILE)) {
            ContainerVerifierResult verifierResult = verifier.verify(container);
            SignatureResult signatureResult = verifierResult.getSignatureResult(container.getSignatureContents().get(0));
            assertEquals(VerificationResult.NOK, verifierResult.getVerificationResult());
            assertNotNull(signatureResult);
            assertEquals(VerificationResult.NOK, signatureResult.getSimplifiedResult());

            com.guardtime.ksi.unisignature.verifier.VerificationResult signatureVerificationFullResults = (com.guardtime.ksi.unisignature.verifier.VerificationResult) signatureResult.getFullResult();
            assertNotNull(signatureVerificationFullResults);
            assertEquals(VerificationErrorCode.GEN_1, signatureVerificationFullResults.getErrorCode());
        }

    }
    @Test
    public void testUsingInternalVerification() throws Exception {
        Policy signatureVerificationPolicy = new InternalVerificationPolicy();
        try (Container container = getContainer(CONTAINER_WITH_WRONG_SIGNATURE_FILE)){
            ContainerVerifier verifier = getContainerVerifier(signatureVerificationPolicy);
            ContainerVerifierResult result = verifier.verify(container);
            assertTrue(result.getVerificationResult().equals(VerificationResult.NOK));
            SignatureResult signatureResult = result.getSignatureResult(container.getSignatureContents().get(0));
            checkExecutedSignatureVerificationPolicy(signatureVerificationPolicy, VerificationResultCode.FAIL, VerificationErrorCode.GEN_1, signatureResult);
        }
    }

    @Test
    public void testUsingCalendarBasedVerification() throws Exception{
        Policy signatureVerificationPolicy = new CalendarBasedVerificationPolicy();
        try (Container container = getContainer(CONTAINER_WITH_CHANGED_SIGNATURE_FILE)) {
            ContainerVerifier verifier = getContainerVerifier(signatureVerificationPolicy);
            ContainerVerifierResult result = verifier.verify(container);
            assertTrue(result.getVerificationResult().equals(VerificationResult.NOK));
            SignatureResult signatureResult = result.getSignatureResult(container.getSignatureContents().get(0));
            checkExecutedSignatureVerificationPolicy(signatureVerificationPolicy, VerificationResultCode.FAIL, VerificationErrorCode.CAL_02, signatureResult);
        }
    }

    @Test
    public void testUsingKeyBasedVerification() throws Exception{
        Policy signatureVerificationPolicy = new KeyBasedVerificationPolicy();
        try (Container container = getContainer(CONTAINER_WITH_CHANGED_SIGNATURE_FILE)) {
            ContainerVerifier verifier = getContainerVerifier(signatureVerificationPolicy);
            ContainerVerifierResult result = verifier.verify(container);
            assertTrue(result.getVerificationResult().equals(VerificationResult.NOK));
            SignatureResult signatureResult = result.getSignatureResult(container.getSignatureContents().get(0));
            checkExecutedSignatureVerificationPolicy(signatureVerificationPolicy, VerificationResultCode.FAIL, VerificationErrorCode.KEY_02, signatureResult);
        }
    }

    @Test
    public void testUsingUserPublicationBasedVerification() throws Exception{
        Policy signatureVerificationPolicy = new UserProvidedPublicationBasedVerificationPolicy();
        try (Container container = getContainer(CONTAINER_WITH_CHANGED_AND_EXTENDED_SIGNATURE_FILE)) {
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
        }
    }

    @Test
    public void testUsingPublicationFileBasedVerification() throws Exception{
        Policy signatureVerificationPolicy = new PublicationsFileBasedVerificationPolicy();
        try (Container container = getContainer(CONTAINER_WITH_CHANGED_SIGNATURE_FILE)){
            ContainerVerifier verifier = getContainerVerifier(signatureVerificationPolicy);
            ContainerVerifierResult result = verifier.verify(container);
            assertTrue(result.getVerificationResult().equals(VerificationResult.NOK));
            SignatureResult signatureResult = result.getSignatureResult(container.getSignatureContents().get(0));
            checkExecutedSignatureVerificationPolicy(signatureVerificationPolicy, VerificationResultCode.FAIL, VerificationErrorCode.PUB_03, signatureResult);
        }
    }

    @Test
    public void testContainerWithValidSignature_VerificationSucceeds() throws Exception {
        try (Container container = getContainer(CONTAINER_WITH_ONE_DOCUMENT)) {
            ContainerVerifierResult verifierResult = verifier.verify(container);

            SignatureResult signatureResult = verifierResult.getSignatureResult(container.getSignatureContents().get(0));
            assertEquals(VerificationResult.OK, verifierResult.getVerificationResult());
            assertNotNull(signatureResult);
            assertEquals(VerificationResult.OK, signatureResult.getSimplifiedResult());
            assertNotNull(signatureResult.getFullResult());
        }
    }

    @Test
    public void testCreateContainerUsingEmptyContainerDocumentAndAddDocumentLater() throws Exception {
        VerificationPolicy verificationPolicy = new DefaultVerificationPolicy(
                defaultRuleStateProvider,
                new KsiSignatureVerifier(ksi, new InternalVerificationPolicy()),
                packagingFactory
        );
        ContainerVerifier containerVerifier = new ContainerVerifier(verificationPolicy);

        String documentName = "Document1.txt";
        byte[] documentContent = "This is document's content.".getBytes();
        Pair<byte[], String> documents = Pair.of(documentContent, documentName);
        try (
                ContainerDocument document = new EmptyContainerDocument(
                        documentName,
                        "txt",
                        Collections.singletonList(new DataHasher(HashAlgorithm.SHA2_256).addData(documentContent).getHash()));
                ContainerAnnotation annotation = new StringContainerAnnotation(
                        ContainerAnnotationType.NON_REMOVABLE,
                        "Document is not with container. Container was created created with empty container document. Document itself can be added later on if needed.",
                        "com.guardtime.com");
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ) {
            try (Container container = packagingFactory.create(Collections.singletonList(document), Collections.singletonList(annotation))) {
                container.writeTo(bos);
            }
            byte[] zipBytes = addDocumentsToExistingContainer_SkipDuplicate(bos.toByteArray(), Collections.singletonList(documents));

            try (Container readin = packagingFactory.read(new ByteArrayInputStream(zipBytes))) {
                ContainerVerifierResult results = containerVerifier.verify(readin);
                assertTrue(results.getVerificationResult().equals(VerificationResult.OK));
            }
        }
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
