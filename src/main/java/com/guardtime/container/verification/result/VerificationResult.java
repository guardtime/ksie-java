package com.guardtime.container.verification.result;

import com.guardtime.container.verification.policy.rule.ContainerRule;

public interface VerificationResult {

    RuleResult getResult();

    ContainerRule getRule();
}
