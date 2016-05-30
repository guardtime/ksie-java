package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.TerminatingVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.ksi.hashing.DataHash;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AnnotationsManifestIntegrityRule extends AbstractRule<SignatureContent> {

    public AnnotationsManifestIntegrityRule() {
        this(RuleState.FAIL);
    }

    public AnnotationsManifestIntegrityRule(RuleState state) {
        super(state);
    }

    @Override
    public List<RuleVerificationResult> verifyRule(SignatureContent verifiable) {
        VerificationResult verificationResult = getFailureVerificationResult();
        AnnotationsManifest annotationsManifest = verifiable.getAnnotationsManifest().getRight();
        Manifest manifest = verifiable.getManifest().getRight();
        FileReference annotationsManifestReference = manifest.getAnnotationsManifestReference();
        try {
            DataHash expectedHash = annotationsManifestReference.getHash();
            DataHash annotationsManifestHash = annotationsManifest.getDataHash(expectedHash.getAlgorithm());
            if(expectedHash.equals(annotationsManifestHash)) {
                verificationResult = VerificationResult.OK;
            }
        } catch (IOException e) {
            LOGGER.debug("Verifying annotations manifest failed!", e);
        }
        TerminatingVerificationResult result = new TerminatingVerificationResult(verificationResult, this, annotationsManifestReference.getUri());
        return Arrays.asList((RuleVerificationResult) result);
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
