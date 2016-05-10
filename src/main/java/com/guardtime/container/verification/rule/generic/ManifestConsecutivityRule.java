package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.util.Util;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.rule.RuleState;

import java.util.LinkedList;
import java.util.List;

/**
 * Rule that verifies that each next {@link Manifest} and its linked manifests have an ascending set of indexes
 * compared to the previous set.
 */
public class ManifestConsecutivityRule extends GenericRule<GenericVerificationResult> {

    private static final String KSIE_VERIFY_MANIFEST_INDEX = "KSIE_VERIFY_MANIFEST_INDEX";

    public ManifestConsecutivityRule() {
        super(KSIE_VERIFY_MANIFEST_INDEX);
    }

    public ManifestConsecutivityRule(RuleState state) {
        super(state, KSIE_VERIFY_MANIFEST_INDEX);
    }

    @Override
    public List<GenericVerificationResult> verify(VerificationContext context) {
        List<GenericVerificationResult> results = new LinkedList<>();
        int expectedIndex = 1;
        for (SignatureContent content : context.getContainer().getSignatureContents()) {
            RuleResult result = getFailureResult();
            Pair<String, Manifest> manifest = content.getManifest();
            int index = Util.extractIntegerFrom(manifest.getLeft());
            if (index == expectedIndex) {
                result = RuleResult.OK;
            }
            expectedIndex = index + 1;
            results.add(new GenericVerificationResult(result, this, manifest.getRight()));
        }
        return results;
    }
}
