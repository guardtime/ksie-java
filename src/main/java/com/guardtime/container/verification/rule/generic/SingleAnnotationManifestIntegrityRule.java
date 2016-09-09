package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.*;

import java.util.Map;

/**
 * This rule verifies the validity of the manifest file containing meta-data for an annotation.
 */
public class SingleAnnotationManifestIntegrityRule extends AbstractRule<Pair<SignatureContent, FileReference>> {

    private static final String NAME = RuleType.KSIE_VERIFY_ANNOTATION.name();

    public SingleAnnotationManifestIntegrityRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
    }

    @Override
    protected void verifyRule(ResultHolder holder, Pair<SignatureContent, FileReference> verifiable) throws RuleTerminatingException {
        FileReference reference = verifiable.getRight();

        Map<String, SingleAnnotationManifest> singleAnnotationManifests = verifiable.getLeft().getSingleAnnotationManifests();
        SingleAnnotationManifest manifest = singleAnnotationManifests.get(reference.getUri());
        ResultHolder tempHolder = new ResultHolder();
        try {
            verifyMultiHashElement(manifest, reference, tempHolder);
        } catch (RuleTerminatingException e) {
            RuleState ruleState = getRuleState(reference);
            RuleVerificationResult result = tempHolder.getResults().get(0);
            if (!result.equals(VerificationResult.OK) && ruleState.equals(RuleState.IGNORE)) {
                // We ignore problems for this manifest
                return;
            }
            holder.addResults(tempHolder.getResults());
            throw e;
        }
    }

    private RuleState getRuleState(FileReference reference) {
        ContainerAnnotationType type = ContainerAnnotationType.fromContent(reference.getMimeType());
        return type.equals(ContainerAnnotationType.FULLY_REMOVABLE) ? RuleState.IGNORE : state;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getErrorMessage() {
        return "Annotation meta-data hash mismatch.";
    }
}
