package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.util.Util;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.ContainerRule;
import com.guardtime.container.verification.rule.RuleType;
import com.guardtime.container.verification.rule.state.RuleStateProvider;

/**
 * This rule verifies that consecutive manifests have appropriate index numbers.
 */
public class ManifestIndexConsistencyRule extends AbstractRule<Container> implements ContainerRule {

    private static final String NAME = RuleType.KSIE_VERIFY_MANIFEST_INDEX.name();

    public ManifestIndexConsistencyRule(RuleStateProvider provider) {
        super(provider.getStateForRule(NAME));
    }

    @Override
    protected void verifyRule(ResultHolder holder, Container verifiable) {
        int expectedIndex = 1;
        for (SignatureContent content : verifiable.getSignatureContents()) {
            VerificationResult result = getFailureVerificationResult();
            Pair<String, Manifest> manifest = content.getManifest();
            int index = Util.extractIntegerFrom(manifest.getLeft());
            if (index == expectedIndex) {
                result = VerificationResult.OK;
            }
            expectedIndex = index + 1;
            holder.addResult(new GenericVerificationResult(result, this, manifest.getLeft()));
        }
    }

    @Override
    public String getErrorMessage() {
        return "Manifest index missing.";
    }

    @Override
    public String getName() {
        return NAME;
    }
}
