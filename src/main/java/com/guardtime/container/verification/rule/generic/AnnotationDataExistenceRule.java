package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.container.verification.rule.RuleType;
import com.guardtime.container.verification.rule.state.RuleState;
import com.guardtime.container.verification.rule.state.RuleStateProvider;

import java.util.LinkedList;
import java.util.List;

import static com.guardtime.container.verification.result.ResultHolder.findHighestPriorityResult;
import static com.guardtime.container.verification.result.VerificationResult.OK;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_EXISTS;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST;
import static com.guardtime.container.verification.rule.RuleType.KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS;

/**
 * This rule verifies that the annotation data is actually present in the {@link com.guardtime.container.packaging.Container}
 */
public class AnnotationDataExistenceRule extends AbstractRule<SignatureContent> {

    private static final String NAME = RuleType.KSIE_VERIFY_ANNOTATION_DATA_EXISTS.getName();

    public AnnotationDataExistenceRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
    }

    @Override
    protected void verifyRule(ResultHolder holder, SignatureContent verifiable) throws RuleTerminatingException {
        for(FileReference reference : verifiable.getAnnotationsManifest().getRight().getSingleAnnotationManifestReferences()) {
            String manifestUri = reference.getUri();
            if(anyRuleFailed(holder, manifestUri)) continue;
            RuleState ruleState = getRuleState(reference);
            VerificationResult result = getFailureVerificationResult();

            String dataPath = getAnnotationDataPath(manifestUri, verifiable);
            ContainerAnnotation annotation = verifiable.getAnnotations().get(dataPath);
            if (annotation != null) {
                result = OK;
            }

            if (!ruleState.equals(RuleState.IGNORE) || result.equals(OK)) {
                holder.addResult(new GenericVerificationResult(result, getName(), getErrorMessage(), dataPath));
            }

        }
    }

    private RuleState getRuleState(FileReference reference) {
        ContainerAnnotationType type = ContainerAnnotationType.fromContent(reference.getMimeType());
        return type.equals(ContainerAnnotationType.NON_REMOVABLE) ? state : RuleState.IGNORE;
    }

    private String getAnnotationDataPath(String manifestUri, SignatureContent signatureContent) {
        SingleAnnotationManifest manifest = signatureContent.getSingleAnnotationManifests().get(manifestUri);
        return manifest.getAnnotationReference().getUri();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getErrorMessage() {
        return "Annotation data missing.";
    }

    @Override
    protected List<RuleVerificationResult> getFilteredResults(ResultHolder holder) {
        List<RuleVerificationResult> filteredResults = new LinkedList<>();
        for (RuleVerificationResult result : holder.getResults()) {
            if (result.getRuleName().equals(KSIE_VERIFY_ANNOTATION_MANIFEST_EXISTS.getName()) ||
                    result.getRuleName().equals(KSIE_VERIFY_ANNOTATION_MANIFEST.getName())) {
                filteredResults.add(result);
            }
        }
        return filteredResults;
    }

    private boolean anyRuleFailed(ResultHolder holder, String uri) {
        List<RuleVerificationResult> filteredResults = new LinkedList<>();
        for (RuleVerificationResult result : holder.getResults()) {
            if (result.getTestedElementPath().equals(uri) &&
                    (result.getRuleName().equals(KSIE_VERIFY_ANNOTATION_EXISTS.getName()) ||
                            result.getRuleName().equals(KSIE_VERIFY_ANNOTATION.getName()))) {
                filteredResults.add(result);
            }
        }
        return filteredResults.isEmpty() || !findHighestPriorityResult(filteredResults).equals(OK);
    }
}
