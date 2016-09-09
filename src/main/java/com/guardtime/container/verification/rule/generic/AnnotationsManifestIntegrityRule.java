package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleState;

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
        AnnotationsManifest annotationsManifest = verifiable.getAnnotationsManifest().getRight();
        Manifest manifest = verifiable.getManifest().getRight();
        FileReference annotationsManifestReference = manifest.getAnnotationsManifestReference();
        return getFileReferenceHashListVerificationResult(annotationsManifest, annotationsManifestReference);
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
