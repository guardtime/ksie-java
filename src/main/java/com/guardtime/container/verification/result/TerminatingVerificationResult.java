package com.guardtime.container.verification.result;

import com.guardtime.container.verification.rule.Rule;

/**
 * A variant of RuleVerificationResult which will indicate the need to stop verification if the result is not {@link
 * VerificationResult#OK}
 */
public class TerminatingVerificationResult extends GenericVerificationResult {

    public TerminatingVerificationResult(VerificationResult result, Rule containerRule, String testedElement, Exception exception) {
        super(result, containerRule, testedElement, exception);
    }

    public TerminatingVerificationResult(VerificationResult result, Rule containerRule, String testedElement) {
        super(result, containerRule, testedElement);
    }

    @Override
    public boolean terminatesVerification() {
        return true;
    }
}

