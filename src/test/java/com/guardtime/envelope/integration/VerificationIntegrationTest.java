/*
 * Copyright 2013-2017 Guardtime, Inc.
 *
 * This file is part of the Guardtime client SDK.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES, CONDITIONS, OR OTHER LICENSES OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * "Guardtime" and "KSI" are trademarks or registered trademarks of
 * Guardtime, Inc., and no license to trademarks is granted; Guardtime
 * reserves and retains all trademark rights.
 */

package com.guardtime.envelope.integration;

import com.guardtime.envelope.annotation.Annotation;
import com.guardtime.envelope.annotation.EnvelopeAnnotationType;
import com.guardtime.envelope.annotation.StringAnnotation;
import com.guardtime.envelope.document.Document;
import com.guardtime.envelope.document.EmptyDocument;
import com.guardtime.envelope.extending.ExtendedEnvelope;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.EnvelopePackagingFactory;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.packaging.zip.ZipEnvelopePackagingFactoryBuilder;
import com.guardtime.envelope.verification.EnvelopeVerifier;
import com.guardtime.envelope.verification.VerifiedEnvelope;
import com.guardtime.envelope.verification.VerifiedSignatureContent;
import com.guardtime.envelope.verification.policy.DefaultVerificationPolicy;
import com.guardtime.envelope.verification.policy.InternalVerificationPolicy;
import com.guardtime.envelope.verification.policy.LimitedInternalVerificationPolicy;
import com.guardtime.envelope.verification.policy.VerificationPolicy;
import com.guardtime.envelope.verification.result.ResultHolder;
import com.guardtime.envelope.verification.result.RuleVerificationResult;
import com.guardtime.envelope.verification.result.SignatureResult;
import com.guardtime.envelope.verification.result.VerificationResult;
import com.guardtime.envelope.verification.rule.signature.ksi.KsiSignatureVerifier;
import com.guardtime.ksi.hashing.DataHasher;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.publication.PublicationData;
import com.guardtime.ksi.unisignature.verifier.PolicyVerificationResult;
import com.guardtime.ksi.unisignature.verifier.VerificationErrorCode;
import com.guardtime.ksi.unisignature.verifier.VerificationResultCode;
import com.guardtime.ksi.unisignature.verifier.policies.CalendarBasedVerificationPolicy;
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
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class VerificationIntegrationTest extends AbstractCommonIntegrationTest {

    private EnvelopeVerifier verifier;
    private VerificationPolicy defaultPolicy;

    @Before
    public void setUpVerifier() {
        this.defaultPolicy = new DefaultVerificationPolicy(
                new KsiSignatureVerifier(ksi, new com.guardtime.ksi.unisignature.verifier.policies.InternalVerificationPolicy())
        );
        this.verifier = new EnvelopeVerifier(defaultPolicy);
    }

    @Test
    public void testVerifiedEnvelopeWithEmptyResults() throws Exception {
        try (Envelope envelope = getEnvelope(ENVELOPE_WITH_NO_DOCUMENTS)) {
            VerifiedEnvelope verifiedEnvelope = new VerifiedEnvelope(envelope, new ResultHolder());
            assertEquals(0, verifiedEnvelope.getResults().size());
            assertEquals(VerificationResult.OK, verifiedEnvelope.getVerificationResult());
        }
    }

    @Test
    public void testVerifyEnvelopeWithNoDocumentUsingLimitedPolicy_OK() throws Exception {
        checkPolicyResult(new LimitedInternalVerificationPolicy(), ENVELOPE_WITH_NO_DOCUMENTS, VerificationResult.OK);
    }

    @Test
    public void testVerifyEnvelopeWithNoDocumentUsingInternalPolicy_NOK() throws Exception {
        checkPolicyResult(new InternalVerificationPolicy(), ENVELOPE_WITH_NO_DOCUMENTS, VerificationResult.NOK);
    }

    @Test
    public void testVerifyEnvelopeWithNoDocumentUsingDefaultPolicy_NOK() throws Exception {
        checkPolicyResult(defaultPolicy, ENVELOPE_WITH_NO_DOCUMENTS, VerificationResult.NOK);
    }

    @Test
    public void testVerifyEnvelopeWithDocumentUsingLimitedPolicy_OK() throws Exception {
        checkPolicyResult(new LimitedInternalVerificationPolicy(), ENVELOPE_WITH_ONE_DOCUMENT, VerificationResult.OK);
    }

    @Test
    public void testVerifyEnvelopeWithDocumentUsingInternalPolicy_OK() throws Exception {
        checkPolicyResult(new InternalVerificationPolicy(), ENVELOPE_WITH_ONE_DOCUMENT, VerificationResult.OK);
    }

    @Test
    public void testVerifyEnvelopeWithDocumentUsingDefaultPolicy_OK() throws Exception {
        checkPolicyResult(defaultPolicy, ENVELOPE_WITH_ONE_DOCUMENT, VerificationResult.OK);
    }

    @Test
    public void testEveryEnvelopeTypeVerifiesOk() throws Exception {
        try (Envelope envelope = getEnvelopeIgnoreExceptions(ENVELOPE_WITH_MULTIPLE_SIGNATURES)) {
            VerifiedEnvelope verifiedEnvelope = verifier.verify(envelope);
            assertEquals(VerificationResult.OK, verifiedEnvelope.getVerificationResult());

            ExtendedEnvelope extendedEnvelope = new ExtendedEnvelope(envelope);
            assertFalse(extendedEnvelope.isFullyExtended());

            VerifiedEnvelope verifiedExtendedEnvelope = verifier.verify(extendedEnvelope);
            assertEquals(VerificationResult.OK, verifiedExtendedEnvelope.getVerificationResult());

            VerifiedEnvelope verifiedVerifiedEnvelope = verifier.verify(verifiedExtendedEnvelope);
            assertEquals(VerificationResult.OK, verifiedVerifiedEnvelope.getVerificationResult());
        }
    }

    @Test
    public void testVerifyingEnvelopeWithValidAndInvalidSignatures() throws Exception {
        try (Envelope envelope = getEnvelopeIgnoreExceptions(ENVELOPE_WITH_MULTI_CONTENT_ONE_SIGNATURE_IS_INVALID)) {
            EnvelopeVerifier verifier = new EnvelopeVerifier(
                    new DefaultVerificationPolicy(new KsiSignatureVerifier(ksi, new KeyBasedVerificationPolicy()))
            );
            VerifiedEnvelope verifiedEnvelope = verifier.verify(envelope);
            for (VerifiedSignatureContent content : verifiedEnvelope.getVerifiedSignatureContents()) {
                String uri = content.getManifest().getSignatureReference().getUri();
                if (uri.equals("META-INF/signature-1.ksi")) {
                    Assert.assertEquals(VerificationResult.OK, content.getVerificationResult());
                } else if (uri.equals("META-INF/signature-01-02-03-04-05.ksi")) {
                    verifyFailingRule(
                            content.getResults(),
                            "KSIE_VERIFY_SIGNATURE",
                            "META-INF/signature-01-02-03-04-05.ksi",
                            "Signature is invalid."
                    );
                    Assert.assertEquals(VerificationResult.NOK, content.getVerificationResult());
                } else {
                    throw new InvalidParameterException("Invalid envelope is provided for test.");
                }
            }
        }
    }

    @Test
    public void testVerifyEnvelopeWithChangedDocument() throws Exception {
        try (Envelope envelope = getEnvelopeIgnoreExceptions(ENVELOPE_WITH_CHANGED_DOCUMENT)) {
            VerifiedEnvelope verifiedEnvelope = verifier.verify(envelope);
            Assert.assertEquals(VerificationResult.NOK, verifiedEnvelope.getVerificationResult());
            verifyFailingRule(verifiedEnvelope.getResults(), "KSIE_VERIFY_DATA_HASH", "test.txt", "Hash mismatch");
        }
    }

    @Test
    public void testVerifyEnvelopeWithChangedSignature() throws Exception {
        try (Envelope envelope = getEnvelopeIgnoreExceptions(ENVELOPE_WITH_MISSING_SIGNATURE)) {
            VerifiedEnvelope verifiedEnvelope = verifier.verify(envelope);
            Assert.assertEquals(VerificationResult.NOK, verifiedEnvelope.getVerificationResult());
            verifyFailingRule(
                    verifiedEnvelope.getResults(),
                    "KSIE_VERIFY_SIGNATURE_EXISTS",
                    "META-INF/signature-1.ksi",
                    "No signature in envelope for manifest!"
            );
        }
    }

    @Test
    public void testVerifyEnvelopeWithOnlyManifest() throws Exception {
        try (Envelope envelope = getEnvelopeIgnoreExceptions(ENVELOPE_WITH_CONTAINS_ONLY_MANIFEST)) {
            VerifiedEnvelope verifiedEnvelope = verifier.verify(envelope);
            Assert.assertEquals(VerificationResult.NOK, verifiedEnvelope.getVerificationResult());
            verifyFailingRule(
                    verifiedEnvelope.getResults(),
                    "KSIE_VERIFY_SIGNATURE_EXISTS",
                    "META-INF/signature-1.ksi",
                    "No signature in envelope for manifest!"
            );
        }
    }

    @Test
    public void testVerifyEnvelopeWithChangedAnnotationData() throws Exception {
        try (Envelope envelope = getEnvelopeIgnoreExceptions(ENVELOPE_WITH_CHANGED_ANNOTATION_DATA)) {
            VerifiedEnvelope verifiedEnvelope = verifier.verify(envelope);
            Assert.assertEquals(VerificationResult.NOK, verifiedEnvelope.getVerificationResult());
            verifyFailingRule(
                    verifiedEnvelope.getResults(),
                    "KSIE_VERIFY_ANNOTATION_DATA",
                    "META-INF/annotation-1.dat",
                    "Annotation data hash mismatch."
            );
        }
    }

    @Test
    public void testVerifyEnvelopeWithChangedDatamanifestHashInManifest() throws Exception {
        try (Envelope envelope = getEnvelopeIgnoreExceptions(ENVELOPE_WITH_CHANGED_DATAMANIFEST_HASH_IN_MANIFEST)) {
            VerifiedEnvelope verifiedEnvelope = verifier.verify(envelope);
            Assert.assertEquals(VerificationResult.NOK, verifiedEnvelope.getVerificationResult());
            verifyFailingRule(
                    verifiedEnvelope.getResults(),
                    "KSIE_VERIFY_DATA_MANIFEST",
                    "META-INF/datamanifest-1.tlv",
                    "Hash mismatch"
            );
        }
    }

    @Test
    public void testVerifyEnvelopeWithChangedAnnotationsManifestHashInManifest() throws Exception {
        try (Envelope envelope = getEnvelopeIgnoreExceptions(ENVELOPE_WITH_CHANGED_ANNOTATIONS_MANIFEST_HASH_IN_MANIFEST)) {
            VerifiedEnvelope verifiedEnvelope = verifier.verify(envelope);
            Assert.assertEquals(VerificationResult.NOK, verifiedEnvelope.getVerificationResult());
            verifyFailingRule(
                    verifiedEnvelope.getResults(),
                    "KSIE_VERIFY_ANNOTATION_MANIFEST",
                    "META-INF/annotmanifest-1.tlv",
                    "Hash mismatch"
            );
        }
    }

    @Test
    public void testVerifyEnvelopeWithChangedDatamanifestHashInAnnotationManifest() throws Exception {
        try (Envelope envelope = getEnvelopeIgnoreExceptions(ENVELOPE_WITH_CHANGED_DATAMANIFEST_HASH_IN_ANNOTATION_MANIFEST)) {
            VerifiedEnvelope verifiedEnvelope = verifier.verify(envelope);
            Assert.assertEquals(VerificationResult.NOK, verifiedEnvelope.getVerificationResult());
            verifyFailingRule(
                    verifiedEnvelope.getResults(),
                    "KSIE_VERIFY_ANNOTATION",
                    "META-INF/datamanifest-1.tlv",
                    "Hash mismatch"
            );
        }
    }

    @Test
    public void testVerifyEnvelopeWithInvalidDatamanifestReferenceInAnnotationManifest() throws Exception {
        try (Envelope envelope = getEnvelopeIgnoreExceptions(ENVELOPE_WITH_INVALID_DATAMANIFEST_HASH_IN_ANNOTATION_MANIFEST)) {
            VerifiedEnvelope verifiedEnvelope = verifier.verify(envelope);
            Assert.assertEquals(VerificationResult.NOK, verifiedEnvelope.getVerificationResult());
            verifyFailingRule(
                    verifiedEnvelope.getResults(),
                    "KSIE_VERIFY_ANNOTATION",
                    "META-INF/annotation-1.tlv",
                    "Annotation meta-data mismatch."
            );
        }
    }

    @Test
    public void testVerifyEnvelopeWithChangedAnnotationManifestHashInAnnotationsManifest() throws Exception {
        try (Envelope envelope =
                     getEnvelopeIgnoreExceptions(ENVELOPE_WITH_CHANGED_ANNOTATION_MANIFEST_HASH_IN_ANNOTATIONS_MANIFEST)) {
            VerifiedEnvelope verifiedEnvelope = verifier.verify(envelope);
            Assert.assertEquals(VerificationResult.NOK, verifiedEnvelope.getVerificationResult());
            verifyFailingRule(
                    verifiedEnvelope.getResults(),
                    "KSIE_VERIFY_ANNOTATION",
                    "META-INF/annotation-1.tlv",
                    "Hash mismatch"
            );
        }
    }

    @Test
    public void testVerifyEnvelopeWithValidAndInvalidContent() throws Exception {
        try (Envelope envelope = getEnvelopeIgnoreExceptions(ENVELOPE_WITH_MULTI_CONTENT_ONE_IS_MISSING_DATAMANIFEST)) {
            VerifiedEnvelope verifiedEnvelope = verifier.verify(envelope);
            Assert.assertEquals(VerificationResult.NOK, verifiedEnvelope.getVerificationResult());
            verifyFailingRule(
                    verifiedEnvelope.getResults(),
                    "KSIE_VERIFY_DATA_MANIFEST_EXISTS",
                    "META-INF/datamanifest-654984984.tlv",
                    "Datamanifest is not present in the envelope."
            );
        }
    }

    @Test
    public void testEnvelopeWithInvalidSignature_VerificationFails() throws Exception {
        try (Envelope envelope = getEnvelopeIgnoreExceptions(ENVELOPE_WITH_WRONG_SIGNATURE_FILE)) {
            VerifiedEnvelope verifiedEnvelope = verifier.verify(envelope);
            SignatureResult signatureResult = verifiedEnvelope
                    .getVerifiedSignatureContents()
                    .get(0)
                    .getSignatureResults()
                    .get(0);
            assertEquals(VerificationResult.NOK, verifiedEnvelope.getVerificationResult());
            assertNotNull(signatureResult);
            assertEquals(VerificationResult.NOK, signatureResult.getSimplifiedResult());

            com.guardtime.ksi.unisignature.verifier.VerificationResult signatureVerificationFullResults =
                    (com.guardtime.ksi.unisignature.verifier.VerificationResult) signatureResult.getFullResult();
            assertNotNull(signatureVerificationFullResults);
            assertEquals(VerificationErrorCode.GEN_01, signatureVerificationFullResults.getErrorCode());
        }
    }

    @Test
    public void testUsingInternalVerification() throws Exception {
        Policy signatureVerificationPolicy = new com.guardtime.ksi.unisignature.verifier.policies.InternalVerificationPolicy();
        try (Envelope envelope = getEnvelopeIgnoreExceptions(ENVELOPE_WITH_WRONG_SIGNATURE_FILE)) {
            EnvelopeVerifier verifier = getEnvelopeVerifier(signatureVerificationPolicy);
            VerifiedEnvelope verifiedEnvelope = verifier.verify(envelope);
            assertTrue(verifiedEnvelope.getVerificationResult().equals(VerificationResult.NOK));
            SignatureResult signatureResult = verifiedEnvelope
                    .getVerifiedSignatureContents()
                    .get(0)
                    .getSignatureResults()
                    .get(0);
            checkExecutedSignatureVerificationPolicy(
                    signatureVerificationPolicy,
                    VerificationResultCode.FAIL,
                    VerificationErrorCode.GEN_01,
                    signatureResult
            );
        }
    }

    @Test
    public void testUsingCalendarBasedVerification() throws Exception {
        Policy signatureVerificationPolicy = new CalendarBasedVerificationPolicy();
        try (Envelope envelope = getEnvelopeIgnoreExceptions(ENVELOPE_WITH_CHANGED_SIGNATURE_FILE)) {
            EnvelopeVerifier verifier = getEnvelopeVerifier(signatureVerificationPolicy);
            VerifiedEnvelope verifiedEnvelope = verifier.verify(envelope);
            assertTrue(verifiedEnvelope.getVerificationResult().equals(VerificationResult.NOK));
            SignatureResult signatureResult = verifiedEnvelope
                    .getVerifiedSignatureContents()
                    .get(0)
                    .getSignatureResults()
                    .get(0);
            checkExecutedSignatureVerificationPolicy(
                    signatureVerificationPolicy,
                    VerificationResultCode.FAIL,
                    VerificationErrorCode.CAL_02,
                    signatureResult
            );
        }
    }

    @Test
    public void testUsingKeyBasedVerification_NOK() throws Exception {
        Policy signatureVerificationPolicy = new KeyBasedVerificationPolicy();
        try (Envelope envelope = getEnvelopeIgnoreExceptions(ENVELOPE_WITH_CHANGED_SIGNATURE_FILE)) {
            EnvelopeVerifier verifier = getEnvelopeVerifier(signatureVerificationPolicy);
            VerifiedEnvelope verifiedEnvelope = verifier.verify(envelope);
            assertTrue(verifiedEnvelope.getVerificationResult().equals(VerificationResult.NOK));
            SignatureResult signatureResult = verifiedEnvelope
                    .getVerifiedSignatureContents()
                    .get(0)
                    .getSignatureResults()
                    .get(0);
            checkExecutedSignatureVerificationPolicy(
                    signatureVerificationPolicy,
                    VerificationResultCode.FAIL,
                    VerificationErrorCode.KEY_02,
                    signatureResult
            );
        }
    }

    @Test
    public void testUsingUserPublicationBasedVerification_NOK() throws Exception {
        Policy signatureVerificationPolicy = new UserProvidedPublicationBasedVerificationPolicy();
        try (Envelope envelope = getEnvelopeIgnoreExceptions(ENVELOPE_WITH_CHANGED_AND_EXTENDED_SIGNATURE_FILE)) {
            VerificationPolicy policy = new DefaultVerificationPolicy(
                    new KsiSignatureVerifier(
                            ksi,
                            signatureVerificationPolicy,
                            new PublicationData(
                                    "AAAAAA-CX4K4D-6AMFWE-EMMHOH-WZT2ZR-Q5MUMQ-DGYCW5-LV5IID-GA672M-LHP5GW-GUGHQN-DA7CGV"
                            )
                    )
            );
            EnvelopeVerifier verifier = new EnvelopeVerifier(policy);
            VerifiedEnvelope verifiedEnvelope = verifier.verify(envelope);
            assertTrue(verifiedEnvelope.getVerificationResult().equals(VerificationResult.NOK));
            SignatureResult signatureResult = verifiedEnvelope
                    .getVerifiedSignatureContents()
                    .get(0)
                    .getSignatureResults()
                    .get(0);
            checkExecutedSignatureVerificationPolicy(
                    signatureVerificationPolicy,
                    VerificationResultCode.FAIL,
                    VerificationErrorCode.PUB_04,
                    signatureResult
            );
        }
    }

    @Test
    public void testUsingPublicationFileBasedVerification() throws Exception {
        Policy signatureVerificationPolicy = new PublicationsFileBasedVerificationPolicy();
        try (Envelope envelope = getEnvelopeIgnoreExceptions(ENVELOPE_WITH_CHANGED_SIGNATURE_FILE)) {
            EnvelopeVerifier verifier = getEnvelopeVerifier(signatureVerificationPolicy);
            VerifiedEnvelope verifiedEnvelope = verifier.verify(envelope);
            assertTrue(verifiedEnvelope.getVerificationResult().equals(VerificationResult.NOK));
            SignatureResult signatureResult = verifiedEnvelope
                    .getVerifiedSignatureContents()
                    .get(0)
                    .getSignatureResults()
                    .get(0);
            checkExecutedSignatureVerificationPolicy(
                    signatureVerificationPolicy,
                    VerificationResultCode.FAIL,
                    VerificationErrorCode.PUB_03,
                    signatureResult
            );
        }
    }

    @Test
    public void testEnvelopeWithValidSignature_VerificationSucceeds() throws Exception {
        VerificationPolicy policy = new DefaultVerificationPolicy(
                new KsiSignatureVerifier(ksi, new CalendarBasedVerificationPolicy())
        );
        EnvelopeVerifier envelopeVerifier = new EnvelopeVerifier(policy);
        try (Envelope envelope = getEnvelopeIgnoreExceptions(ENVELOPE_WITH_ONE_DOCUMENT)) {
            VerifiedEnvelope verifiedEnvelope = envelopeVerifier.verify(envelope);

            SignatureResult signatureResult = verifiedEnvelope
                    .getVerifiedSignatureContents()
                    .get(0)
                    .getSignatureResults()
                    .get(0);
            assertEquals(VerificationResult.OK, verifiedEnvelope.getVerificationResult());
            assertNotNull(signatureResult);
            assertEquals(VerificationResult.OK, signatureResult.getSimplifiedResult());
            assertNotNull(signatureResult.getFullResult());
        }
    }

    @Test
    public void testCreateEnvelopeUsingEmptyEnvelopeDocumentAndAddDocumentLater() throws Exception {
        byte[] documentContent = "This is document's content.".getBytes(StandardCharsets.UTF_8);
        baseTestCreateEnvelopeUsingEmptyEnvelopeDocumentAndAddDocumentData(
                VerificationResult.OK,
                documentContent,
                documentContent
        );
    }

    @Test
    public void testCreateEnvelopeUsingEmptyEnvelopeDocumentAndAddWrongDocumentLater() throws Exception {
        byte[] documentContent = "This is document's content.".getBytes(StandardCharsets.UTF_8);
        baseTestCreateEnvelopeUsingEmptyEnvelopeDocumentAndAddDocumentData(
                VerificationResult.NOK,
                documentContent,
                "IncorrectContent".getBytes()
        );
    }

    @Test
    public void testAttachDocumentAndVerify_VerificationSuccessful() throws Exception {
        VerifiedEnvelope verifiedEnvelope = null;
        Document detached = null;
        InputStream inputStream = null;
        try (Envelope envelope = getEnvelopeIgnoreExceptions(ENVELOPE_WITH_ONE_DOCUMENT)) {
            SignatureContent content = envelope.getSignatureContents().get(0);
            String documentName = content.getDocuments().get(content.getDocuments().keySet().iterator().next()).getFileName();

            verifiedEnvelope = verifier.verify(envelope);
            assertEquals(VerificationResult.OK, verifiedEnvelope.getVerificationResult());

            detached = content.detachDocument(documentName);
            verifiedEnvelope = verifier.verify(envelope);
            assertEquals(VerificationResult.NOK, verifiedEnvelope.getVerificationResult());

            inputStream = detached.getInputStream();
            boolean added = content.attachDetachedDocument(detached.getFileName(), inputStream);
            assertTrue(added);
            verifiedEnvelope = verifier.verify(envelope);
            assertEquals(VerificationResult.OK, verifiedEnvelope.getVerificationResult());
            added = content.attachDetachedDocument(detached.getFileName(), inputStream);
            assertFalse(added);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }

            if (detached != null) {
                detached.close();
            }

            if (verifiedEnvelope != null) {
                verifiedEnvelope.close();
            }
        }
    }

    private void checkPolicyResult(VerificationPolicy policy, String envelopeName, VerificationResult result) throws Exception {
        EnvelopeVerifier verifier = new EnvelopeVerifier(policy);
        try (Envelope envelope = getEnvelope(envelopeName)) {
            VerifiedEnvelope verifiedEnvelope = verifier.verify(envelope);
            assertEquals(result, verifiedEnvelope.getVerificationResult());
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

    private EnvelopeVerifier getEnvelopeVerifier(Policy policy) {
        VerificationPolicy envelopePolicy = new DefaultVerificationPolicy(
                new KsiSignatureVerifier(ksi, policy)
        );
        return new EnvelopeVerifier(envelopePolicy);
    }

    private void checkExecutedSignatureVerificationPolicy(Policy expectedPolicy, VerificationResultCode expectedResultCode,
                                                          VerificationErrorCode expectedErrorCode, SignatureResult result)
            throws Exception {
        com.guardtime.ksi.unisignature.verifier.VerificationResult signatureVerificationResults =
                (com.guardtime.ksi.unisignature.verifier.VerificationResult) result.getFullResult();
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

    private void baseTestCreateEnvelopeUsingEmptyEnvelopeDocumentAndAddDocumentData(
            VerificationResult verificationResult, byte[] expectedDocumentContent, byte[] addedDocumentContent) throws Exception {
        EnvelopeVerifier envelopeVerifier =
                getEnvelopeVerifier(new com.guardtime.ksi.unisignature.verifier.policies.InternalVerificationPolicy());
        EnvelopePackagingFactory packagingFactory = new ZipEnvelopePackagingFactoryBuilder()
                .withSignatureFactory(signatureFactory)
                .withVerificationPolicy(new LimitedInternalVerificationPolicy())
                .build();
        String documentName = "Document1.txt";
        try (
                Document document = new EmptyDocument(
                        documentName,
                        "txt",
                        singletonList(new DataHasher(HashAlgorithm.SHA2_256).addData(expectedDocumentContent).getHash()));
                Annotation annotation = new StringAnnotation(
                        EnvelopeAnnotationType.NON_REMOVABLE,
                        "Document is not with envelope. Envelope was created with empty envelope document. " +
                                "Document itself can be added later on if needed.",
                        "com.guardtime.com");
                Envelope envelope = packagingFactory.create(singletonList(document), singletonList(annotation));
                InputStream stream = new ByteArrayInputStream(addedDocumentContent)
        ) {
            VerifiedEnvelope verifiedEnvelope = envelopeVerifier.verify(envelope);
            assertTrue(verifiedEnvelope.getVerificationResult().equals(VerificationResult.NOK));

            envelope.getSignatureContents().get(0).attachDetachedDocument(documentName, stream);
            verifiedEnvelope = envelopeVerifier.verify(envelope);
            assertTrue(verifiedEnvelope.getVerificationResult().equals(verificationResult));
        }
    }
}
