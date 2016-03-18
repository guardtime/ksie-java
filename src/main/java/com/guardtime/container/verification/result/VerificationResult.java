package com.guardtime.container.verification.result;

import com.guardtime.container.verification.policy.rule.VerificationRule;

public interface VerificationResult {

    Object getTested();

    RuleResult getResult();

    VerificationRule getRule();
}
