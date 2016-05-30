package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.RuleState;

public abstract class AbstractRule<O> extends AbstractRuleHelper<O> implements Rule<O> {

    protected AbstractRule(RuleState state) {
        super(state);
    }

}
