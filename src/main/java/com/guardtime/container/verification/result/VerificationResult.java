package com.guardtime.container.verification.result;

import com.guardtime.container.verification.policy.rule.Rule;

public interface VerificationResult {

    Object getTested();

    RuleResult getResult();

    Rule getRule();
}
