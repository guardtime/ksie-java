package com.guardtime.container.integration;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.annotation.StringContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.EmptyContainerDocument;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.verification.ContainerVerifier;
import com.guardtime.container.verification.VerifiedContainer;
import com.guardtime.container.verification.VerifiedSignatureContent;
import com.guardtime.container.verification.policy.DefaultVerificationPolicy;
import com.guardtime.container.verification.policy.VerificationPolicy;
import com.guardtime.container.verification.result.RuleVerificationResult;
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class VerificationIntegrationTest extends AbstractCommonIntegrationTest {

    private ContainerVerifier verifier;

    @Before
    public void setUpVerifier() {
        VerificationPolicy defaultPolicy = new DefaultVerificationPolicy(
                new KsiSignatureVerifier(ksi, new CalendarBasedVerificationPolicy()),
                packagingFactory
        );
        this.verifier = new ContainerVerifier(defaultPolicy);
    }

    @Test
    public void testVerifyingContainerWithValidAndInvalidSignatures() throws Exception {
        try (Container container = getContainerIgnoreExceptions(CONTAINER_WITH_MULTI_CONTENT_ONE_SIGNATURE_IS_INVALID)) {
            ContainerVerifier verifier = new ContainerVerifier(new DefaultVerificationPolicy(new KsiSignatureVerifier(ksi, new KeyBasedVerificationPolicy()), packagingFactory));
            VerifiedContainer verifiedContainer = verifier.verify(container);
            for (VerifiedSignatureContent content : verifiedContainer.getVerifiedSignatureContents()) {
                if (content.getManifest().getRight().getSignatureReference().getUri().equals("META-INF/signature-1.ksi")) {
                    Assert.assertEquals(VerificationResult.OK, content.getVerificationResult());
                } else if (content.getManifest().getRight().getSignatureReference().getUri().equals("META-INF/signature-01-02-03-04-05.ksi")) {
                    verifyFailingRule(content.getResults(), "KSIE_VERIFY_MANIFEST", "META-INF/signature-01-02-03-04-05.ksi", "Signature mismatch.");
                    Assert.assertEquals(VerificationResult.NOK, content.getVerificationResult());
                } else {
                    throw new InvalidParameterException("Invalid container is provided for test.");
                }
            }
        }
    }

    @Test
    public void testVerifyContainerWithChangedDocument() throws Exception {
        try (Container container = getContainerIgnoreExceptions(CONTAINER_WITH_CHANGED_DOCUMENT)) {
            VerifiedContainer verifiedContainer = verifier.verify(container);
            Assert.assertEquals(VerificationResult.NOK, verifiedContainer.getVerificationResult());
            verifyFailingRule(verifiedContainer.getResults(), "KSIE_VERIFY_DATA_HASH", "test.txt", "Hash mismatch");
        }
    }

    @Test
    public void testVerifyContainerWithChangedSignature() throws Exception {
        try (Container container = getContainerIgnoreExceptions(CONTAINER_WITH_MISSING_SIGNATURE)) {
            VerifiedContainer verifiedContainer = verifier.verify(container);
            Assert.assertEquals(VerificationResult.NOK, verifiedContainer.getVerificationResult());
            verifyFailingRule(verifiedContainer.getResults(), "KSIE_VERIFY_SIGNATURE_EXISTS", "META-INF/signature-1.ksi", "No signature in container for manifest!");
        }
    }

    @Test
    public void testVerifyContainerWithOnlyManifest() throws Exception {
        try (Container container = getContainerIgnoreExceptions(CONTAINER_WITH_CONTAINS_ONLY_MANIFEST)) {
            VerifiedContainer verifiedContainer = verifier.verify(container);
            Assert.assertEquals(VerificationResult.NOK, verifiedContainer.getVerificationResult());
            verifyFailingRule(verifiedContainer.getResults(), "KSIE_VERIFY_SIGNATURE_EXISTS", "META-INF/signature-1.ksi", "No signature in container for manifest!");
        }
    }

    @Test
    public void testVerifyContainerWithChangedAnnotationData() throws Exception {
        try (Container container = getContainerIgnoreExceptions(CONTAINER_WITH_CHANGED_ANNOTATION_DATA)) {
            VerifiedContainer verifiedContainer = verifier.verify(container);
            Assert.assertEquals(VerificationResult.NOK, verifiedContainer.getVerificationResult());
            verifyFailingRule(verifiedContainer.getResults(), "KSIE_VERIFY_ANNOTATION_DATA", "META-INF/annotation-1.dat", "Annotation data hash mismatch.");
        }
    }

    @Test
    public void testVerifyContainerWithMimetypeContainingInvalidValue() throws Exception {
        try (Container container = getContainerIgnoreExceptions(CONTAINER_WITH_MIMETYPE_CONTAINS_INVALID_VALUE)) {
            VerifiedContainer verifiedContainer = verifier.verify(container);
            Assert.assertEquals(VerificationResult.NOK, verifiedContainer.getVerificationResult());
            verifyFailingRule(verifiedContainer.getResults(), "KSIE_FORMAT", "mimetype", "Unsupported format.");
        }
    }

    @Test
    public void testVerifyContainerWithMimetypeContainingMoreThanNeeded() throws Exception {
        try (Container container = getContainerIgnoreExceptions(CONTAINER_WITH_MIMETYPE_CONTAINS_ADDITIONAL_VALUE)) {
            VerifiedContainer verifiedContainer = verifier.verify(container);
            Assert.assertEquals(VerificationResult.NOK, verifiedContainer.getVerificationResult());
            verifyFailingRule(verifiedContainer.getResults(), "KSIE_FORMAT", "mimetype", "Unsupported format.");
        }
    }

    @Test
    public void testVerifyContainerWithChangedDatamanifestHashInManifest() throws Exception {
        try (Container container = getContainerIgnoreExceptions(CONTAINER_WITH_CHANGED_DATAMANIFEST_HASH_IN_MANIFEST)) {
            VerifiedContainer verifiedContainer = verifier.verify(container);
            Assert.assertEquals(VerificationResult.NOK, verifiedContainer.getVerificationResult());
            verifyFailingRule(verifiedContainer.getResults(), "KSIE_VERIFY_DATA_MANIFEST", "META-INF/datamanifest-1.tlv", "Hash mismatch");
        }
    }

    @Test
    public void testVerifyContainerWithChangedAnnotationsManifestHashInManifest() throws Exception {
        try (Container container = getContainerIgnoreExceptions(CONTAINER_WITH_CHANGED_ANNOTATIONS_MANIFEST_HASH_IN_MANIFEST)) {
            VerifiedContainer verifiedContainer = verifier.verify(container);
            Assert.assertEquals(VerificationResult.NOK, verifiedContainer.getVerificationResult());
            verifyFailingRule(verifiedContainer.getResults(), "KSIE_VERIFY_ANNOTATION_MANIFEST", "META-INF/annotmanifest-1.tlv", "Hash mismatch");
        }
    }

    @Test
    public void testVerifyContainerWithChangedDatamanifestHashInAnnotationManifest() throws Exception {
        try (Container container = getContainerIgnoreExceptions(CONTAINER_WITH_CHANGED_DATAMANIFEST_HASH_IN_ANNOTATION_MANIFEST)) {
            VerifiedContainer verifiedContainer = verifier.verify(container);
            Assert.assertEquals(VerificationResult.NOK, verifiedContainer.getVerificationResult());
            verifyFailingRule(verifiedContainer.getResults(), "KSIE_VERIFY_ANNOTATION", "META-INF/datamanifest-1.tlv", "Hash mismatch");
        }
    }

    @Test
    public void testVerifyContainerWithInvalidDatamanifestReferenceInAnnotationManifest() throws Exception {
        try (Container container = getContainerIgnoreExceptions(CONTAINER_WITH_INVALID_DATAMANIFEST_HASH_IN_ANNOTATION_MANIFEST)) {
            VerifiedContainer verifiedContainer = verifier.verify(container);
            Assert.assertEquals(VerificationResult.NOK, verifiedContainer.getVerificationResult());
            verifyFailingRule(verifiedContainer.getResults(), "KSIE_VERIFY_ANNOTATION", "META-INF/annotation-1.tlv", "Annotation meta-data mismatch.");
        }
    }

    @Test
    public void testVerifyContainerWithChangedAnnotationManifestHashInAnnotationsManifest() throws Exception {
        try (Container container = getContainerIgnoreExceptions(CONTAINER_WITH_CHANGED_ANNOTATION_MANIFEST_HASH_IN_ANNOTATIONS_MANIFEST)) {
            VerifiedContainer verifiedContainer = verifier.verify(container);
            Assert.assertEquals(VerificationResult.NOK, verifiedContainer.getVerificationResult());
            verifyFailingRule(verifiedContainer.getResults(), "KSIE_VERIFY_ANNOTATION", "META-INF/annotation-1.tlv", "Hash mismatch");
        }
    }

    @Test
    public void testVerifyContainerWithValidAndInvalidContent() throws Exception {
        try (Container container = getContainerIgnoreExceptions(CONTAINER_WITH_MULTI_CONTENT_ONE_IS_MISSING_DATAMANIFEST)) {
            VerifiedContainer verifiedContainer = verifier.verify(container);
            Assert.assertEquals(VerificationResult.NOK, verifiedContainer.getVerificationResult());
            verifyFailingRule(verifiedContainer.getResults(), "KSIE_VERIFY_DATA_MANIFEST_EXISTS", "META-INF/datamanifest-654984984.tlv", "Datamanifest is not present in the container.");
        }
    }

    @Test
    public void testContainerWithInvalidSignature_VerificationFails() throws Exception {
        try (Container container = getContainerIgnoreExceptions(CONTAINER_WITH_WRONG_SIGNATURE_FILE)) {
            VerifiedContainer verifierResult = verifier.verify(container);
            SignatureResult signatureResult = verifierResult.getVerifiedSignatureContents().get(0).getSignatureResults().get(0);
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
        try (Container container = getContainerIgnoreExceptions(CONTAINER_WITH_WRONG_SIGNATURE_FILE)) {
            ContainerVerifier verifier = getContainerVerifier(signatureVerificationPolicy);
            VerifiedContainer result = verifier.verify(container);
            assertTrue(result.getVerificationResult().equals(VerificationResult.NOK));
            SignatureResult signatureResult = result.getVerifiedSignatureContents().get(0).getSignatureResults().get(0);
            checkExecutedSignatureVerificationPolicy(signatureVerificationPolicy, VerificationResultCode.FAIL, VerificationErrorCode.GEN_1, signatureResult);
        }
    }

    @Test
    public void testUsingCalendarBasedVerification() throws Exception {
        Policy signatureVerificationPolicy = new CalendarBasedVerificationPolicy();
        try (Container container = getContainerIgnoreExceptions(CONTAINER_WITH_CHANGED_SIGNATURE_FILE)) {
            ContainerVerifier verifier = getContainerVerifier(signatureVerificationPolicy);
            VerifiedContainer result = verifier.verify(container);
            assertTrue(result.getVerificationResult().equals(VerificationResult.NOK));
            SignatureResult signatureResult = result.getVerifiedSignatureContents().get(0).getSignatureResults().get(0);
            checkExecutedSignatureVerificationPolicy(signatureVerificationPolicy, VerificationResultCode.FAIL, VerificationErrorCode.CAL_02, signatureResult);
        }
    }

    @Test
    public void testUsingKeyBasedVerification() throws Exception {
        Policy signatureVerificationPolicy = new KeyBasedVerificationPolicy();
        try (Container container = getContainerIgnoreExceptions(CONTAINER_WITH_CHANGED_SIGNATURE_FILE)) {
            ContainerVerifier verifier = getContainerVerifier(signatureVerificationPolicy);
            VerifiedContainer result = verifier.verify(container);
            assertTrue(result.getVerificationResult().equals(VerificationResult.NOK));
            SignatureResult signatureResult = result.getVerifiedSignatureContents().get(0).getSignatureResults().get(0);
            checkExecutedSignatureVerificationPolicy(signatureVerificationPolicy, VerificationResultCode.FAIL, VerificationErrorCode.KEY_02, signatureResult);
        }
    }

    @Test
    public void testUsingUserPublicationBasedVerification() throws Exception {
        Policy signatureVerificationPolicy = new UserProvidedPublicationBasedVerificationPolicy();
        try (Container container = getContainerIgnoreExceptions(CONTAINER_WITH_CHANGED_AND_EXTENDED_SIGNATURE_FILE)) {
            VerificationPolicy policy = new DefaultVerificationPolicy(
                    new KsiSignatureVerifier(ksi, signatureVerificationPolicy, new PublicationData("AAAAAA-CX4K4D-6AMFWE-EMMHOH-WZT2ZR-Q5MUMQ-DGYCW5-LV5IID-GA672M-LHP5GW-GUGHQN-DA7CGV")),
                    packagingFactory
            );
            ContainerVerifier verifier = new ContainerVerifier(policy);
            VerifiedContainer result = verifier.verify(container);
            assertTrue(result.getVerificationResult().equals(VerificationResult.NOK));
            SignatureResult signatureResult = result.getVerifiedSignatureContents().get(0).getSignatureResults().get(0);
            checkExecutedSignatureVerificationPolicy(signatureVerificationPolicy, VerificationResultCode.FAIL, VerificationErrorCode.INT_09, signatureResult);
        }
    }

    @Test
    public void testUsingPublicationFileBasedVerification() throws Exception {
        Policy signatureVerificationPolicy = new PublicationsFileBasedVerificationPolicy();
        try (Container container = getContainerIgnoreExceptions(CONTAINER_WITH_CHANGED_SIGNATURE_FILE)) {
            ContainerVerifier verifier = getContainerVerifier(signatureVerificationPolicy);
            VerifiedContainer result = verifier.verify(container);
            assertTrue(result.getVerificationResult().equals(VerificationResult.NOK));
            SignatureResult signatureResult = result.getVerifiedSignatureContents().get(0).getSignatureResults().get(0);
            checkExecutedSignatureVerificationPolicy(signatureVerificationPolicy, VerificationResultCode.FAIL, VerificationErrorCode.PUB_03, signatureResult);
        }
    }

    @Test
    public void testContainerWithValidSignature_VerificationSucceeds() throws Exception {
        try (Container container = getContainerIgnoreExceptions(CONTAINER_WITH_ONE_DOCUMENT)) {
            VerifiedContainer verifierResult = verifier.verify(container);

            SignatureResult signatureResult = verifierResult.getVerifiedSignatureContents().get(0).getSignatureResults().get(0);
            assertEquals(VerificationResult.OK, verifierResult.getVerificationResult());
            assertNotNull(signatureResult);
            assertEquals(VerificationResult.OK, signatureResult.getSimplifiedResult());
            assertNotNull(signatureResult.getFullResult());
        }
    }

    @Test
    public void testCreateContainerUsingEmptyContainerDocumentAndAddDocumentLater() throws Exception {
        byte[] documentContent = "This is document's content.".getBytes(StandardCharsets.UTF_8);
        baseTestCreateContainerUsingEmptyContainerDocumentAndAddDocumentData(VerificationResult.OK, documentContent, documentContent);
    }

    @Test
    public void testCreateContainerUsingEmptyContainerDocumentAndAddWrongDocumentLater() throws Exception {
        byte[] documentContent = "This is document's content.".getBytes(StandardCharsets.UTF_8);
        baseTestCreateContainerUsingEmptyContainerDocumentAndAddDocumentData(VerificationResult.NOK, documentContent, "IncorrectContent".getBytes());
    }

    @Test
    public void testAttachDocumentAndVerify_VerificationSuccessful() throws Exception {
        VerifiedContainer verifiedContainer = null;
        ContainerDocument detached = null;
        try (Container container = getContainerIgnoreExceptions(CONTAINER_WITH_ONE_DOCUMENT)) {
            SignatureContent content = container.getSignatureContents().get(0);
            String documentName = content.getDocuments().get(content.getDocuments().keySet().iterator().next()).getFileName();

            verifiedContainer = verifier.verify(container);
            assertEquals(VerificationResult.OK, verifiedContainer.getVerificationResult());

            detached = content.detachDocument(documentName);
            verifiedContainer = verifier.verify(container);
            assertEquals(VerificationResult.NOK, verifiedContainer.getVerificationResult());

            boolean added = content.attachDetachedDocument(detached.getFileName(), detached.getInputStream());
            assertTrue(added);
            verifiedContainer = verifier.verify(container);
            assertEquals(VerificationResult.OK, verifiedContainer.getVerificationResult());
            added = content.attachDetachedDocument(detached.getFileName(), detached.getInputStream());
            assertFalse(added);
        } finally {
            if(detached != null) {
                detached.close();
            }

            if (verifiedContainer != null) {
                verifiedContainer.close();
            }
        }
    }

    private void baseTestCreateContainerUsingEmptyContainerDocumentAndAddDocumentData(VerificationResult verificationResult, byte[] expectedDocumentContent, byte[] addedDocumentContent) throws Exception {
        ContainerVerifier containerVerifier = getContainerVerifier(new InternalVerificationPolicy());

        String documentName = "Document1.txt";
        try (
                ContainerDocument document = new EmptyContainerDocument(
                        documentName,
                        "txt",
                        Collections.singletonList(new DataHasher(HashAlgorithm.SHA2_256).addData(expectedDocumentContent).getHash()));
                ContainerAnnotation annotation = new StringContainerAnnotation(
                        ContainerAnnotationType.NON_REMOVABLE,
                        "Document is not with container. Container was created created with empty container document. Document itself can be added later on if needed.",
                        "com.guardtime.com");
                Container container = packagingFactory.create(Collections.singletonList(document), Collections.singletonList(annotation));
                InputStream stream = new ByteArrayInputStream(addedDocumentContent)
        ) {
            VerifiedContainer results = containerVerifier.verify(container);
            assertTrue(results.getVerificationResult().equals(VerificationResult.NOK));

            container.getSignatureContents().get(0).attachDetachedDocument(documentName, stream);
            results = containerVerifier.verify(container);
            assertTrue(results.getVerificationResult().equals(verificationResult));
        }
    }

    private void verifyFailingRule(List<RuleVerificationResult> results, String ruleName, String testedElement, String message) {
        for (RuleVerificationResult result : results) {
            if (result.getRuleName().equals(ruleName) &&
                    result.getTestedElementPath().equals(testedElement) &&
                    result.getRuleErrorMessage().equals(message)) {
                Assert.assertEquals(VerificationResult.NOK, result.getVerificationResult());
            } else {
                Assert.assertEquals(VerificationResult.OK, result.getVerificationResult());
            }
        }
    }

    private ContainerVerifier getContainerVerifier(Policy policy) {
        VerificationPolicy containerPolicy = new DefaultVerificationPolicy(
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
