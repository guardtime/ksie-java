package com.guardtime.container.verification.result;

import com.guardtime.container.ContainerFileElement;

public interface RuleVerificationResult {

    RuleResult getResult();

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
