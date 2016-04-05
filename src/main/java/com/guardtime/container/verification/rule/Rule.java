package com.guardtime.container.verification.rule;

import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.result.RuleVerificationResult;

import java.util.List;

public interface Rule<O extends RuleVerificationResult> {

    /**
     * @param context
     * @return List of results of the test
     */
    List<O> verify(VerificationContext context);

    String getName();
}