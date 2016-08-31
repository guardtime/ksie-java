package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;

/**
 * This rule verifies that the annotmanifest has not been corrupted.
 */
public class AnnotationsManifestIntegrityRule extends AbstractRule<SignatureContent> {

    public AnnotationsManifestIntegrityRule(RuleState state) {
        super(state);
    }

    @Override
    protected void verifyRule(ResultHolder holder, SignatureContent verifiable) throws RuleTerminatingException {
        VerificationResult verificationResult = getFailureVerificationResult();
        AnnotationsManifest annotationsManifest = verifiable.getAnnotationsManifest().getRight();
        Manifest manifest = verifiable.getManifest().getRight();
        FileReference annotationsManifestReference = manifest.getAnnotationsManifestReference();
        String annotationsManifestUri = annotationsManifestReference.getUri();
        try {
            for (DataHash expectedHash : annotationsManifestReference.getHashList()) {
                if (expectedHash.getAlgorithm().getStatus() != HashAlgorithm.Status.NORMAL) {
                    continue; // Skip not implemented or not trusted
                }
                DataHash annotationsManifestHash = annotationsManifest.getDataHash(expectedHash.getAlgorithm());
                if (expectedHash.equals(annotationsManifestHash)) {
                    verificationResult = VerificationResult.OK;
                }
            }
            holder.addResult(new GenericVerificationResult(verificationResult, this, annotationsManifestUri));
        } catch (IOException e) {
            LOGGER.info("Verifying annotations manifest failed!", e);
            holder.addResult(new GenericVerificationResult(verificationResult, this, annotationsManifestUri, e));
        }

        if (!verificationResult.equals(VerificationResult.OK)) {
            throw new RuleTerminatingException("AnnotationsManifest integrity could not be verified for '" + annotationsManifestUri + "'");
        }
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
