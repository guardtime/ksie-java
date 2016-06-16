package com.guardtime.container.verification.result;

/**
 * Results produced during verification which group together the rule {@link VerificationResult} and rule data like rule
 * name and error message and path of the tested container component.
 */
public interface RuleVerificationResult {

    VerificationResult getVerificationResult();

    /**
     * Indicates which rule was used to produce this result by referring to the rules unique name string.
     */
    String getRuleName();

    /**
     * Contains the message string provided by the rule which applies for a non OK result.
     */
    String getRuleErrorMessage();

    /**
     * Provides path of the element that the verification was performed on by the rule. This is a helper for
     * sorting/distinguishing between results for different elements contained in the container.
     */
    String getTestedElementPath();

    /**
     * Indicates if the verification process should be terminated in case of failure result.
     */
    boolean terminatesVerification();
}
