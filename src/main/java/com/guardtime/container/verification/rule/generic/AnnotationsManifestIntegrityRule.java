package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.util.Util;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.rule.RuleState;
import com.guardtime.ksi.hashing.DataHash;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class AnnotationsManifestIntegrityRule extends SignatureContentRule {

    private static final String KSIE_VERIFY_ANNOTATIONS_MANIFEST = "KSIE_VERIFY_ANNOTATIONS_MANIFEST";

    public AnnotationsManifestIntegrityRule() {
        super(KSIE_VERIFY_ANNOTATIONS_MANIFEST);
    }

    public AnnotationsManifestIntegrityRule(RuleState state) {
        super(state, KSIE_VERIFY_ANNOTATIONS_MANIFEST);
    }

    @Override
    protected List<Pair<? extends Object, ? extends RuleVerificationResult>> verifySignatureContent(SignatureContent content, VerificationContext context) {
        List<Pair<? extends Object, ? extends RuleVerificationResult>> results = new LinkedList<>();
        RuleResult result = getFailureResult();
        FileReference annotationsManifestReference = content.getSignatureManifest().getRight().getAnnotationsManifestReference();
        try {
            AnnotationsManifest annotationsManifest = content.getAnnotationsManifest().getRight();
            DataHash expectedDataHash = annotationsManifestReference.getHash();
            DataHash realHash = Util.hash(annotationsManifest.getInputStream(), expectedDataHash.getAlgorithm());
            if (expectedDataHash.equals(realHash)) {
                result = RuleResult.OK;
            }
        } catch (NullPointerException | IOException e) {
            // TODO: log exception?
        }
        results.add(Pair.of(annotationsManifestReference, new GenericVerificationResult(result, this)));
        return results;
    }

}
