package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.util.Util;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.RuleState;

import java.util.LinkedList;
import java.util.List;

/**
 * This rule verifies that consecutive manifests have appropriate index numbers.
 */
public class ManifestIndexConsistencyRule extends AbstractContainerRule {

    public ManifestIndexConsistencyRule() {
        this(RuleState.FAIL);
    }

    public ManifestIndexConsistencyRule(RuleState state) {
        super(state);
    }

    @Override
    protected List<RuleVerificationResult> verifyRule(Container verifiable) {
        List<RuleVerificationResult> results = new LinkedList<>();
        int expectedIndex = 1;
        for (SignatureContent content : verifiable.getSignatureContents()) {
            VerificationResult result = getFailureVerificationResult();
            Pair<String, Manifest> manifest = content.getManifest();
            int index = Util.extractIntegerFrom(manifest.getLeft());
            if (index == expectedIndex) {
                result = VerificationResult.OK;
            }
            expectedIndex = index + 1;
            results.add(new GenericVerificationResult(result, this, manifest.getLeft()));
        }
        return results;
    }

    @Override
    public String getErrorMessage() {
        return "Manifest index missing.";
    }

    @Override
    public String getName() {
        return "KSIE_VERIFY_MANIFEST_INDEX";
    }
}
