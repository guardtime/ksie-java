package com.guardtime.container.verification.result;

import com.guardtime.container.verification.policy.rule.ContainerRule;

public interface VerificationResult {

    Object getTested();

    RuleResult getResult();

    ContainerRule getRule();
}
