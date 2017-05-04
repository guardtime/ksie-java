package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.container.verification.rule.state.RuleStateProvider;

import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS;

/**
 * This rule verifies that the annotations manifest is actually present in the {@link
 * com.guardtime.container.packaging.Container}
 */
public class AnnotationsManifestExistenceRule extends AbstractRule<SignatureContent> {

    private static final String NAME = KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS.getName();

    public AnnotationsManifestExistenceRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
    }

    @Override
    protected void verifyRule(ResultHolder holder, SignatureContent verifiable) throws RuleTerminatingException {
        VerificationResult verificationResult = getFailureVerificationResult();
        Manifest manifest = verifiable.getManifest().getRight();
        FileReference annotationsManifestReference = manifest.getAnnotationsManifestReference();
        String annotationsManifestUri = annotationsManifestReference.getUri();
        Pair<String, AnnotationsManifest> annotationsManifest = verifiable.getAnnotationsManifest();
        if (annotationsManifest != null && annotationsManifest.getLeft().equals(annotationsManifestUri)) {
            verificationResult = VerificationResult.OK;
        }
        holder.addResult(
                verifiable,
                new GenericVerificationResult(verificationResult, getName(), getErrorMessage(), annotationsManifestUri)
        );
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getErrorMessage() {
        return "Annotations manifest is not present in the container.";
    }
}
