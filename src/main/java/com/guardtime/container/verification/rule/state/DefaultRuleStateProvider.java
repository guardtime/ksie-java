package com.guardtime.container.verification.rule.state;

/**
 * Simple implementation of {@link RuleStateProvider} that returns {@link RuleState#FAIL} for everything.
 */
public class DefaultRuleStateProvider implements RuleStateProvider {

    @Override
    public RuleState getStateForRule(String name) {
        return RuleState.FAIL;
    }
}
