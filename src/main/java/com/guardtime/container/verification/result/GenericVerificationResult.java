package com.guardtime.container.verification.result;

import com.guardtime.container.verification.policy.rule.ContainerRule;

public class GenericVerificationResult implements VerificationResult {
    private final RuleResult result;
    private final ContainerRule rule;
    private final Object tested;

    public GenericVerificationResult(RuleResult result, ContainerRule rule, Object tested) {
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
    public ContainerRule getRule() {
        return rule;
    }
}
