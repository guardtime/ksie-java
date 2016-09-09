package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.ksi.hashing.DataHash;

import java.util.List;
import java.util.Map;

/**
 * This rule verifies the validity of the manifest file containing meta-data for an annotation.
 */
public class SingleAnnotationManifestIntegrityRule extends AbstractRule<Pair<SignatureContent, FileReference>> {

    public SingleAnnotationManifestIntegrityRule() {
        this(RuleState.FAIL);
    }

    public SingleAnnotationManifestIntegrityRule(RuleState state) {
        super(state);
    }

    @Override
    protected List<RuleVerificationResult> verifyRule(Pair<SignatureContent, FileReference> verifiable) {
        FileReference fileReference = verifiable.getRight();
        Map<String, SingleAnnotationManifest> singleAnnotationManifests = verifiable.getLeft().getSingleAnnotationManifests();
        SingleAnnotationManifest manifest = singleAnnotationManifests.get(fileReference.getUri());
        return getFileReferenceHashListVerificationResult(manifest, fileReference);
    }

    @Override
    public String getName() {
        return "KSIE_VERIFY_ANNOTATION";
    }

    @Override
    public String getErrorMessage() {
        return "Annotation meta-data hash mismatch.";
    }
}
