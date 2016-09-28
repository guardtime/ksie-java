package com.guardtime.container.verification.rule.state;

import com.guardtime.container.verification.rule.RuleType;

public interface RuleStateProvider {

    /**
     * Returns proper {@link RuleState} for given rule name. Known and common rule names are in {@link RuleType} enum.
     */
    RuleState getStateForRule(String name);
}
