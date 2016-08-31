package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.container.verification.rule.RuleTerminatingException;

import java.util.Map;

/**
 * This rule verifies the existence of the meta-data file of an annotation.
 */
public class SingleAnnotationManifestExistenceRule extends AbstractRule<Pair<SignatureContent, FileReference>> {

    public SingleAnnotationManifestExistenceRule(RuleState ruleState) {
        super(ruleState);
    }

    @Override
    protected void verifyRule(ResultHolder holder, Pair<SignatureContent, FileReference> verifiable) throws RuleTerminatingException {
        FileReference reference = verifiable.getRight();
        ContainerAnnotationType type = ContainerAnnotationType.fromContent(reference.getMimeType());
        RuleState ruleState = type.equals(ContainerAnnotationType.FULLY_REMOVABLE) ? RuleState.IGNORE : state;

        String manifestUri = reference.getUri();
        Map<String, SingleAnnotationManifest> singleAnnotationManifests = verifiable.getLeft().getSingleAnnotationManifests();
        SingleAnnotationManifest manifest = singleAnnotationManifests.get(manifestUri);
        VerificationResult result = getFailureVerificationResult();
        if (manifest != null) {
            result = VerificationResult.OK;
        }

        if (!ruleState.equals(RuleState.IGNORE) || result.equals(VerificationResult.OK)) {
            holder.addResult(new GenericVerificationResult(result, this, manifestUri));
        }

        if (!result.equals(VerificationResult.OK)) {
            throw new RuleTerminatingException("SingleAnnotationManifest existence could not be verified for '" + manifestUri + "'");
        }
    }

    @Override
    public String getName() {
        return "KSIE_VERIFY_ANNOTATION_EXISTS";
    }

    @Override
    public String getErrorMessage() {
        return "Annotation meta-data missing.";
    }
}
