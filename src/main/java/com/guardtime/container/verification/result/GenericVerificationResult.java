package com.guardtime.container.verification.result;

import com.guardtime.container.verification.policy.rule.VerificationRule;

public class GenericVerificationResult implements VerificationResult {
    private final RuleResult result;
    private final VerificationRule rule;
    private final Object tested;

    public GenericVerificationResult(RuleResult result, VerificationRule rule, Object tested) {
        this.result = result;
        this.rule = rule;
        this.tested = tested;
    }

    @Override
    public Object getTested() {
        return tested;
    }

    @Override
    public RuleResult getResult() {
        return result;
    }

    @Override
    public VerificationRule getRule() {
        return rule;
    }
}
