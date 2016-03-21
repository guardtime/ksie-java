package com.guardtime.container.verification.result;

import com.guardtime.container.verification.rule.Rule;

public class TerminatingVerificationResult implements VerificationResult {
    private final RuleResult result;
    private final String rule;
    private final Object tested;

    public TerminatingVerificationResult(RuleResult result, Rule rule, Object tested) {
        this.result = result;
        this.rule = rule.getName();
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
    public String getRuleName() {
        return rule;
    }

    @Override
    public boolean terminatesVerification() {
        return true;
    }
}

