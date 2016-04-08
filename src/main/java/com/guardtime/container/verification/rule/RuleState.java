package com.guardtime.container.verification.rule;

public enum RuleState {
    FAIL("RULE_FAIL"),
    WARN("RULE_WARN"),
    IGNORE("RULE_IGNORE");

    private final String name;

    RuleState(String name) {
        this.name = name;
    }
}
