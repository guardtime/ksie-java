package com.guardtime.container.verification.result;

/**
 * Possible results for any given rule used to verify a container.
 */
public enum VerificationResult {
    OK("RESULT_OK", 0),
    WARN("RESULT_WARN", 1),
    NOK("RESULT_NOK", 2);

    private final String name;
    private final int weight;

    VerificationResult(String name, int weight) {
        this.name = name;
        this.weight = weight;
    }

    /**
     * Compares this with that to determine which hash higher priority.
     * @param that
     * @return true when this has higher priority than that.
     */
    public boolean isMoreImportantThan(VerificationResult that) {
        return this.weight > that.weight;
    }
}
