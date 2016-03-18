package com.guardtime.container.verification.policy.rule;

import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.result.VerificationResult;

import java.util.List;

public interface ContainerRule extends Rule{

    List<VerificationResult> verify(VerificationContext context);

    /**
     * States if the rule is to be ignored.
     * Convenience method to prevent manually checking against RuleState.IGNORE
     * Also provides the ability to ignore the rule if a previously processed rules result makes it unnecessary
     * @param previousResults - Results from previously run rules. May be empty.
     * @return - Boolean stating whether running the rule won't provide additional valuable information.
     */
    boolean shouldBeIgnored(List<VerificationResult> previousResults);
}