package com.guardtime.container.verification.result;

import com.guardtime.container.ContainerFileElement;

public interface RuleVerificationResult {

    RuleResult getResult();

    /**
     * Indicates which rule was used to produce this result by referring to the rules unique name string.
     *
     * @return
     */
    String getRuleName();

    /**
     * Indicates if the verification process should be terminated in case of failure result.
     *
     * @return
     */
    boolean terminatesVerification();

    /**
     * Provides the element that the verification was performed on by the rule. This is a helper for
     * sorting/distinguishing between results for different elements contained in the container.
     *
     * @return
     */
    ContainerFileElement getTestedElement();
}
