package com.guardtime.container.verification.result;

import com.guardtime.container.verification.rule.Rule;

public class TerminatingVerificationResult extends GenericVerificationResult {

    public TerminatingVerificationResult(RuleResult result, Rule rule) {
        super(result, rule);
    }

    @Override
    public boolean terminatesVerification() {
        return true;
    }
}

