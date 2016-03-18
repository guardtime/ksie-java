package com.guardtime.container.verification.result;

import com.guardtime.container.verification.policy.rule.Rule;

public interface VerificationResult {

    RuleResult getResult();

    Rule getRule();
}
