package com.guardtime.container.verification.rule;

import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.result.VerificationResult;

import java.util.List;

public interface SignatureContentRule extends Rule{

    List<? extends VerificationResult> verify(SignatureContent content, VerificationContext context);

    /**
     * States if the rule is to be ignored.
     * Convenience method to prevent manually checking against RuleState.IGNORE
     * Also provides the ability to ignore the rule if a previously processed rules result makes it unnecessary
     * @param content    Currently verifiable SignatureContent e.g. complete data structure associated with a signature
     * @param context    Currently verifiable context containing complete container and results of performed rules
     * @return - Boolean stating whether running the rule won't provide additional valuable information.
     */
    boolean shouldBeIgnored(SignatureContent content, VerificationContext context);
}
