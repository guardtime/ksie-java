package com.guardtime.container.verification.result;

public enum RuleResult {
    OK("RESULT_OK", 0),
    WARN("RESULT_WARN", 1),
    NOK("RESULT_NOK", 2);

    private final String name;
    private final int weight;

    RuleResult(String name, int weight) {
        this.name = name;
        this.weight = weight;
    }

    public boolean isMoreImportant(RuleResult that) {
        return this.weight > that.weight;
    }
}
