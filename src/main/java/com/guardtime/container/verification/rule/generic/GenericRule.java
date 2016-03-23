package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.RuleState;

import java.util.List;

public abstract class GenericRule implements Rule {
    protected final RuleState state;

    public GenericRule(RuleState state) {
        this.state = state;
    }

    @Override
    public boolean shouldBeIgnored(List<RuleVerificationResult> previousResults) {
        return RuleState.IGNORE.equals(state);
    }

    protected RuleResult getFailureResult() {
        return RuleState.WARN.equals(state) ? RuleResult.WARN : RuleResult.NOK;
    }
}
