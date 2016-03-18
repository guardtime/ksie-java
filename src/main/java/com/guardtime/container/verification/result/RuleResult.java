package com.guardtime.container.verification.result;

public enum RuleResult {
    OK("RESULT_OK", 0),
    WARN("RESULT_WARN", 1),
    NOK("RESULT_NOK", 2);

    private final String name;
    private final int value;

    RuleResult(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public boolean isMoreImportant(RuleResult that) {
        return this.value > that.value;
    }
}
