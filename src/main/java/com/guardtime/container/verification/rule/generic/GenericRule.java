package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.RuleState;

public abstract class GenericRule implements Rule {
    protected final RuleState state;
    protected final String name;

    public GenericRule(String name) {
        this(RuleState.FAIL, name);
    }

    public GenericRule(RuleState state, String name) {
        this.state = state;
        this.name = name;
    }

    protected RuleResult getFailureResult() {
        return RuleState.WARN.equals(state) ? RuleResult.WARN : RuleResult.NOK;
    }

    @Override
    public String getName() {
        return name;
    }
}
