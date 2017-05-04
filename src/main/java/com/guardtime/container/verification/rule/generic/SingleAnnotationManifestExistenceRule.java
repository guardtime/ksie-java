package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.result.VerificationResultFilter;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.container.verification.rule.state.RuleState;
import com.guardtime.container.verification.rule.state.RuleStateProvider;

import java.util.HashSet;
import java.util.Set;

import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_EXISTS;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS;

/**
 * This rule verifies the existence of the meta-data file of an annotation.
 * It expects to find successful results for rules verifying existence and integrity of
 * {@link com.guardtime.container.manifest.AnnotationsManifest}.
 */
public class SingleAnnotationManifestExistenceRule extends AbstractRule<SignatureContent> {

    private static final String NAME = KSIE_VERIFY_ANNOTATION_EXISTS.getName();

    public SingleAnnotationManifestExistenceRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
    }

    @Override
    protected void verifyRule(ResultHolder holder, SignatureContent verifiable) throws RuleTerminatingException {
        for (FileReference annotationReference : verifiable.getAnnotationsManifest().getRight().getSingleAnnotationManifestReferences()) {
            ContainerAnnotationType type = ContainerAnnotationType.fromContent(annotationReference.getMimeType());
            RuleState ruleState = type.equals(ContainerAnnotationType.FULLY_REMOVABLE) ? RuleState.IGNORE : state;

            String manifestUri = annotationReference.getUri();
            SingleAnnotationManifest manifest = verifiable.getSingleAnnotationManifests().get(manifestUri);
            VerificationResult result = getFailureVerificationResult();
            if (manifest != null) {
                result = VerificationResult.OK;
            }

            if (!ruleState.equals(RuleState.IGNORE) || result.equals(VerificationResult.OK)) {
                holder.addResult(verifiable, new GenericVerificationResult(result, getName(), getErrorMessage(), manifestUri));
            }
        }

    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getErrorMessage() {
        return "Annotation meta-data missing.";
    }

    @Override
    protected VerificationResultFilter getFilter(ResultHolder holder, SignatureContent verifiable) {
        final Set<RuleVerificationResult> results = new HashSet<>(holder.getResults(verifiable));
        return new VerificationResultFilter() {
            @Override
            public boolean apply(RuleVerificationResult result) {
                return results.contains(result) && (result.getRuleName().equals(KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS.getName()) ||
                        result.getRuleName().equals(KSIE_VERIFY_ANNOTATION_MANIFEST.getName()));
            }
        };
    }

}
