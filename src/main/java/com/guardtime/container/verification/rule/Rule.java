package com.guardtime.container.verification.rule;

import com.guardtime.container.verification.result.RuleVerificationResult;

import java.util.List;

public interface Rule<O extends Object> {

    /**
     * @param verifiable object
     * @return List of results of the test
     */
    List<RuleVerificationResult> verify(O verifiable);

    /**
     *
     * @return Unique string which can be used to identify the type of the rule.
     */
    String getName();

    String getErrorMessage();
}
