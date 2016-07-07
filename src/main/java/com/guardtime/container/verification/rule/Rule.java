package com.guardtime.container.verification.rule;

import com.guardtime.container.verification.result.RuleVerificationResult;

import java.util.List;

/**
 * Rule to be performed during verification of {@link com.guardtime.container.packaging.Container} or its components.
 * @param <O>   Verifiable object class.
 */
public interface Rule<O extends Object> {

    /**
     * Verifies {@link O} to produce a list of {@link RuleVerificationResult}. Depending on the implementation, there
     * can be nested Rules used during verification.
     * @param verifiable object to be examined
     * @return List of results gathered from verifying
     */
    List<RuleVerificationResult> verify(O verifiable);

    /**
     * Returns unique string which can be used to identify the type of the rule.
     */
    String getName();

    /**
     * Returns error string of the rule that applies when the rule results in a not OK state.
     */
    String getErrorMessage();
}
