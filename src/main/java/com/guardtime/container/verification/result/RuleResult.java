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

    /**
     * Compares this with that to determine which hash higher priority.
     * @param that
     * @return true when this has higher priority than that.
     */
    public boolean isMoreImportantThan(RuleResult that) {
        return this.weight > that.weight;
    }
}
