package com.guardtime.container.verification.rule;

public class DefaultRuleStateProvider implements RuleStateProvider {

    @Override
    public RuleState getStateForRule(String name) {
        return RuleState.FAIL;
    }
}
