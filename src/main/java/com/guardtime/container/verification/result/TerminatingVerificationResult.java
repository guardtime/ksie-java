package com.guardtime.container.verification.result;

import com.guardtime.container.ContainerFileElement;
import com.guardtime.container.verification.rule.Rule;

/**
 * A variant of RuleVerificationResult which will indicate the need to stop verification if the result is not {@link
 * RuleResult#OK}
 */
public class TerminatingVerificationResult extends GenericVerificationResult {

    public TerminatingVerificationResult(RuleResult result, Rule rule, ContainerFileElement testedElement) {
        super(result, rule, testedElement);
    }

    @Override
    public boolean terminatesVerification() {
        return true;
    }
}

