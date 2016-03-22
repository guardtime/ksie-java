package com.guardtime.container.verification.result;

public class GenericVerificationResult implements RuleVerificationResult {
    private final RuleResult result;
    private final String rule;
    private final Object tested;

    public GenericVerificationResult(RuleResult result, String rule, Object tested) {
        this.result = result;
        this.rule = rule;
        this.tested = tested;
    }

    @Override
    public Object getTested() { // TODO: Improve as Object is too dangerous and ambiguous
        return tested;
    }

    @Override
    public RuleResult getResult() {
        return result;
    }

    @Override
    public String getRuleName() {
        return rule;
    }

    @Override
    public boolean terminatesVerification() {
        return false;
    }
}
