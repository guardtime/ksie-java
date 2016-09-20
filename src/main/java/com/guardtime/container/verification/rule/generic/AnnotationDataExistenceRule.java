package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.*;

/**
 * This rule verifies that the annotation data is actually present in the {@link com.guardtime.container.packaging.Container}
 */
public class AnnotationDataExistenceRule extends AbstractRule<Pair<SignatureContent, FileReference>> {

    private static final String NAME = RuleType.KSIE_VERIFY_ANNOTATION_DATA_EXISTS.name();

    public AnnotationDataExistenceRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
    }

    @Override
    protected void verifyRule(ResultHolder holder, Pair<SignatureContent, FileReference> verifiable) throws RuleTerminatingException {
        FileReference reference = verifiable.getRight();
        SignatureContent signatureContent = verifiable.getLeft();
        RuleState ruleState = getRuleState(reference);
        VerificationResult result = getFailureVerificationResult();

        String dataPath = getAnnotationDataPath(reference, signatureContent);
        ContainerAnnotation annotation = signatureContent.getAnnotations().get(dataPath);
        if (annotation != null) {
            result = VerificationResult.OK;
        }

        if (!ruleState.equals(RuleState.IGNORE) || result.equals(VerificationResult.OK)) {
            holder.addResult(new GenericVerificationResult(result, this, dataPath));
        }

        if (!result.equals(VerificationResult.OK)) {
            throw new RuleTerminatingException("AnnotationData existence could not be verified for '" + dataPath + "'");
        }
    }

    private RuleState getRuleState(FileReference reference) {
        ContainerAnnotationType type = ContainerAnnotationType.fromContent(reference.getMimeType());
        return type.equals(ContainerAnnotationType.NON_REMOVABLE) ? state : RuleState.IGNORE;
    }

    private String getAnnotationDataPath(FileReference reference, SignatureContent signatureContent) {
        String manifestUri = reference.getUri();
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
}
