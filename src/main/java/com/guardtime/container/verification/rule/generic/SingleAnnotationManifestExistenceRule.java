package com.guardtime.container.verification.rule.generic;

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
import java.util.List;
import java.util.Map;

/**
 * This rule verifies the existence of the meta-data file of an annotation.
 */
public class SingleAnnotationManifestExistenceRule extends AbstractRule<Pair<SignatureContent, FileReference>> {
    public SingleAnnotationManifestExistenceRule(RuleState ruleState) {
        super(ruleState);
    }

    @Override
    protected List<RuleVerificationResult> verifyRule(Pair<SignatureContent, FileReference> verifiable) {
        String manifestUri = verifiable.getRight().getUri();
        Map<String, SingleAnnotationManifest> singleAnnotationManifests = verifiable.getLeft().getSingleAnnotationManifests();
        SingleAnnotationManifest manifest = singleAnnotationManifests.get(manifestUri);
        VerificationResult result = getFailureVerificationResult();
        if (manifest != null) {
            result = VerificationResult.OK;
        }
        RuleVerificationResult verificationResult = new TerminatingVerificationResult(result, this, manifestUri);
        return Arrays.asList(verificationResult);
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
