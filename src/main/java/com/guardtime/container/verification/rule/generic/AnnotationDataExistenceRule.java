package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.TerminatingVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleState;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * This rule verifies that the annotation data is actually present in the {@link com.guardtime.container.packaging.Container}
 */
public class AnnotationDataExistenceRule extends AbstractRule<Pair<SignatureContent, FileReference>> {

    public AnnotationDataExistenceRule() {
        this(RuleState.FAIL);
    }

    public AnnotationDataExistenceRule(RuleState ruleState) {
        super(ruleState);
    }

    @Override
    protected List<RuleVerificationResult> verifyRule(Pair<SignatureContent, FileReference> verifiable) {
        if (ignoreRule()) return new LinkedList<>();
        VerificationResult result = getFailureVerificationResult();
        String manifestUri = verifiable.getRight().getUri();
        SignatureContent signatureContent = verifiable.getLeft();
        SingleAnnotationManifest manifest = signatureContent.getSingleAnnotationManifests().get(manifestUri);
        String dataUri = manifest.getAnnotationReference().getUri();
        ContainerAnnotation annotation = signatureContent.getAnnotations().get(dataUri);
        if (annotation != null) {
            result = VerificationResult.OK;
        }
        RuleVerificationResult verificationResult = new TerminatingVerificationResult(result, this, dataUri);
        return Arrays.asList(verificationResult);
    }

    @Override
    public String getName() {
        return "KSIE_VERIFY_ANNOTATION_DATA_EXISTS";
    }

    @Override
    public String getErrorMessage() {
        return "Annotation data missing.";
    }
}
