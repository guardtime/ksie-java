package com.guardtime.container.verification.result;

import com.guardtime.container.verification.policy.rule.VerificationRule;

public interface VerificationResult {

    RuleResult getResult();

    VerificationRule getRule();
}
