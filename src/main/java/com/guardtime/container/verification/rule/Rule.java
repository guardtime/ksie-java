package com.guardtime.container.verification.rule;

import com.guardtime.container.verification.result.RuleVerificationResult;

import java.util.List;

/**
 * Rule to be performed during verification of {@link com.guardtime.container.packaging.Container} or its components.
 * @param <O>   Verifiable object class.
 */
public interface Rule<O extends Object> {

    /**
     * Verifies {@link O} to produce a list of {@link RuleVerificationResult}. Any number of nested {@link Rule}s are
     * possible to produce the desired verification.
     * @param verifiable object to be examined
     * @return List of results gathered from verifying passed in object
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
