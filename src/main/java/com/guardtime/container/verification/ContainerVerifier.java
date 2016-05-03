package com.guardtime.container.verification;

import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.policy.VerificationPolicy;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerifierResult;
import com.guardtime.container.verification.rule.Rule;

import java.util.List;

/**
 * Helper class to verify {@link VerificationContext} based on a {@link VerificationPolicy}
 */
public class ContainerVerifier {

    private VerificationPolicy policy;

    public ContainerVerifier(VerificationPolicy policy) {
        this.policy = policy;
    }

    /**
     * Verifies the {@link VerificationContext} based on the rules provided by the {@link VerificationPolicy}. Appends
     * results from rules to the pre-existing list of results contained in the context
     *
     * @param context
     *         containing verifiable container and a list of results from performed rules
     * @return {@link VerifierResult} based on all {@link RuleVerificationResult} gathered during verification.
     */
    public VerifierResult verify(VerificationContext context) {
        for (Rule rule : policy.getRules()) {
            List<RuleVerificationResult> verificationResults = rule.verify(context);
            context.addResults(verificationResults);
            if (terminateVerification(verificationResults)) break;
        }
        return new VerifierResult(context);
    }

    private boolean terminateVerification(List<RuleVerificationResult> verificationResults) {
        for (RuleVerificationResult result : verificationResults) {
            if (result.terminatesVerification() && !RuleResult.OK.equals(result.getResult())) {
                return true;
            }
        }
        return false;
    }


}
