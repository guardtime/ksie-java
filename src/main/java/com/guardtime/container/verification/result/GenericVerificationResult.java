package com.guardtime.container.verification.result;

import com.guardtime.container.verification.rule.Rule;

public class GenericVerificationResult implements RuleVerificationResult {
    private final RuleResult result;
    private final String ruleName;

    public GenericVerificationResult(RuleResult result, Rule rule) {
        this.result = result;
        this.ruleName = rule.getName();
    }

    @Override
    public RuleResult getResult() {
        return result;
    }

    @Override
    public String getRuleName() {
        return ruleName;
    }

    @Override
    public boolean terminatesVerification() {
        return false;
    }
}
