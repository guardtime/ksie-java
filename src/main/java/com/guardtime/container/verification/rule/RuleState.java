package com.guardtime.container.verification.rule;

/**
 * Rule states define how the rule should be handled as some states require the rule to be ignored while others require
 * the result to not invalidate the verification.
 */
public enum RuleState {
    FAIL("RULE_FAIL"),
    WARN("RULE_WARN"),
    IGNORE("RULE_IGNORE");

    private final String name;

    RuleState(String name) {
        this.name = name;
    }
}
