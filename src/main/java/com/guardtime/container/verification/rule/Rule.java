package com.guardtime.container.verification.rule;

import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.RuleVerificationResult;

/**
 * Rule to be performed during verification of {@link com.guardtime.container.packaging.Container} or its components.
 * @param <V> Verifiable object class.
 */
public interface Rule<V> {

    /**
     * Verifies {@link V} to produce a list of {@link RuleVerificationResult} which are added to the provided {@link
     * ResultHolder}. Depending on the implementation, there can be nested Rules used during verification.
     * @param verifiable object to be examined
     * @param resultHolder that maintains all rule verification results
     * @return True unless the verification process is ignored.
     */
    boolean verify(ResultHolder resultHolder, V verifiable) throws RuleTerminatingException;

    /**
     * Returns unique string which can be used to identify the type of the rule.
     */
    String getName();

    /**
     * Returns error string of the rule that applies when the rule results in a not OK state.
     */
    String getErrorMessage();
}
