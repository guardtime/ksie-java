package com.guardtime.container.verification.result;

public interface RuleVerificationResult {

    VerificationResult getResultStatus();

    /**
     * Indicates which rule was used to produce this result by referring to the rules unique name string.
     *
     * @return
     */
    String getRuleName();
    String getRuleErrorMessage();

    /**
     * Provides path of the element that the verification was performed on by the rule. This is a helper for
     * sorting/distinguishing between results for different elements contained in the container.
     *
     * @return
     */
    String getTestedElementPath();

    /**
     * Indicates if the verification process should be terminated in case of failure result.
     *
     * @return
     */
    boolean terminatesVerification();
}
