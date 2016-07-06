package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.TerminatingVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * This rule verifies that the annotmanifest has not been corrupted.
 */
public class AnnotationsManifestIntegrityRule extends AbstractRule<SignatureContent> {

    public AnnotationsManifestIntegrityRule() {
        this(RuleState.FAIL);
    }

    public AnnotationsManifestIntegrityRule(RuleState state) {
        super(state);
    }

    @Override
    protected List<RuleVerificationResult> verifyRule(SignatureContent verifiable) {
        RuleVerificationResult result;
        VerificationResult verificationResult = getFailureVerificationResult();
        AnnotationsManifest annotationsManifest = verifiable.getAnnotationsManifest().getRight();
        Manifest manifest = verifiable.getManifest().getRight();
        FileReference annotationsManifestReference = manifest.getAnnotationsManifestReference();
        try {
            for (DataHash expectedHash : annotationsManifestReference.getHashList()) {
                if (expectedHash.getAlgorithm().getStatus() != HashAlgorithm.Status.NORMAL) {
                    LOGGER.info("Will not perform hash verification for '{}' because algorithm status is '{}'", expectedHash, expectedHash.getAlgorithm().getStatus());
                    continue; // Skip not implemented or not trusted hashes
                }
                DataHash annotationsManifestHash = annotationsManifest.getDataHash(expectedHash.getAlgorithm());
                if (expectedHash.equals(annotationsManifestHash)) {
                    verificationResult = VerificationResult.OK;
                } else {
                    LOGGER.warn("Generated hash does not match hash in reference. Expecting '{}', got '{}'", expectedHash, annotationsManifestHash);
                }
            }
            result = new TerminatingVerificationResult(verificationResult, this, annotationsManifestReference.getUri());
        } catch (IOException e) {
            LOGGER.info("Verifying annotations manifest failed!", e);
            result = new TerminatingVerificationResult(verificationResult, this, annotationsManifestReference.getUri(), e);
        }
        return Arrays.asList(result);
    }

    @Override
    public String getName() {
        return "KSIE_VERIFY_ANNOTATION_MANIFEST";
    }

    @Override
    public String getErrorMessage() {
        return "Annotation manifest hash mismatch.";
    }
}
