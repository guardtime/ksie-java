package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.RuleState;

/**
 * General methods and logic common to rules.
 * @param <O>   Verifiable object class.
 */
public abstract class AbstractRule<O> extends AbstractRuleHelper<O> implements Rule<O> {

    protected AbstractRule(RuleState state) {
        super(state);
    }

}
