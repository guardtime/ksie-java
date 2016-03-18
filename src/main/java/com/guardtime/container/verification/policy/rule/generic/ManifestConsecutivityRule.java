package com.guardtime.container.verification.policy.rule.generic;

import com.guardtime.container.manifest.SignatureManifest;
import com.guardtime.container.packaging.BlockChainContainer;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.util.Util;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.policy.rule.RuleState;
import com.guardtime.container.verification.policy.rule.VerificationRule;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.VerificationResult;

import java.util.LinkedList;
import java.util.List;

public class ManifestConsecutivityRule implements VerificationRule {
    private final RuleState state;

    public ManifestConsecutivityRule() {
        state = RuleState.FAIL;
    }

    public ManifestConsecutivityRule(RuleState state) {
        this.state = state;
    }

    @Override
    public List<VerificationResult> verify(VerificationContext context) {
        BlockChainContainer container = context.getContainer();
        List<VerificationResult> results = new LinkedList<>();
        int expectedIndex = 1;
        for (SignatureContent content : container.getSignatureContents()) {
            RuleResult result = RuleResult.OK;
            Pair<String, SignatureManifest> manifest = content.getSignatureManifest();
            int index = Util.extractIntegerFrom(manifest.getLeft());
            if (index != expectedIndex) {
                result = getFailureResult();
            }
            expectedIndex = index + 1;
            results.add(new GenericVerificationResult(result, this, manifest));
        }
        return results;
    }

    @Override
    public boolean shouldBeIgnored(List<VerificationResult> previousResults) {
        return state == RuleState.IGNORE;
    }

    @Override
    public RuleState getState() {
        return state;
    }

    private RuleResult getFailureResult() {
        return getState() == RuleState.WARN ? RuleResult.WARN : RuleResult.NOK;
    }
}
