package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.MultiHashElement;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.container.verification.rule.RuleType;
import com.guardtime.container.verification.rule.state.RuleState;
import com.guardtime.container.verification.rule.state.RuleStateProvider;

import java.util.Map;

/**
 * This rule verifies the validity of the manifest file containing meta-data for an annotation.
 */
public class SingleAnnotationManifestIntegrityRule extends AbstractRule<Pair<SignatureContent, FileReference>> {

    private static final String NAME = RuleType.KSIE_VERIFY_ANNOTATION.getName();
    private final MultiHashElementIntegrityRule multiHashElementIntegrityRule;

    public SingleAnnotationManifestIntegrityRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
        multiHashElementIntegrityRule = new MultiHashElementIntegrityRule(stateProvider.getStateForRule(NAME), NAME);
    }

    @Override
    protected void verifyRule(ResultHolder holder, Pair<SignatureContent, FileReference> verifiable) throws RuleTerminatingException {
        FileReference reference = verifiable.getRight();

        Map<String, SingleAnnotationManifest> singleAnnotationManifests = verifiable.getLeft().getSingleAnnotationManifests();
        SingleAnnotationManifest manifest = singleAnnotationManifests.get(reference.getUri());
        MultiHashElement documentsManifest = verifiable.getLeft().getDocumentsManifest().getRight();
        FileReference documentsManifestReference = manifest.getDocumentsManifestReference();
        ResultHolder tempHolder = new ResultHolder();
        try {
            multiHashElementIntegrityRule.verify(tempHolder, Pair.of((MultiHashElement) manifest, reference));
            multiHashElementIntegrityRule.verifyRule(tempHolder, Pair.of(documentsManifest, documentsManifestReference));
        } finally {
            RuleState ruleState = getRuleState(reference);
            for (RuleVerificationResult result : tempHolder.getResults()) {
                if (!result.getVerificationResult().equals(VerificationResult.OK) && ruleState.equals(RuleState.IGNORE)) {
                    // We ignore problems
                    continue;
                }
                holder.addResult(result);
            }
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
