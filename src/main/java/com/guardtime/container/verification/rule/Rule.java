package com.guardtime.container.verification.rule;

import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.result.RuleVerificationResult;

import java.util.List;

public interface Rule {

    /**
     * @param context
     * @return List of Pairs of container object tested by the rule and the result of the test
     */
    List<Pair<? extends Object, ? extends RuleVerificationResult>> verify(VerificationContext context);

    String getName();
}