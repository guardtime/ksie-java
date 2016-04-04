package com.guardtime.container.verification;

import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.policy.VerificationPolicy;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerifierResult;
import com.guardtime.container.verification.rule.Rule;

import java.util.List;

public class ContainerVerifier {

    private VerificationPolicy policy;

    public ContainerVerifier(VerificationPolicy policy) {
        this.policy = policy;
    }

    /**
     * Verifies the {@link VerificationContext} based on the rules provided by the {@link VerificationPolicy}. Appends results from
     * rules to the pre-existing list of results contained in the context
     *
     * @param context
     *         containing verifiable container and a list of results from performed rules
     * @return {@link VerifierResult} based on all {@link RuleVerificationResult} gathered during verification.
     */
    public VerifierResult verify(VerificationContext context) {
        for (Rule rule : policy.getRules()) {
            List<Pair<? extends Object, ? extends RuleVerificationResult>> verificationResults = rule.verify(context);
            context.addResults(verificationResults);
            if (terminateVerification(verificationResults)) break;
        }
        return new VerifierResult(context);
    }

    private boolean terminateVerification(List<Pair<? extends Object, ? extends RuleVerificationResult>> verificationResults) {
        for (Pair<? extends Object, ? extends RuleVerificationResult> result : verificationResults) {
            if (resultTerminates(result.getRight())) return true;
        }
        return false;
    }

    private boolean resultTerminates(RuleVerificationResult result) {
        if (!result.terminatesVerification()) return false;
        return !RuleResult.OK.equals(result.getResult());
    }

}
