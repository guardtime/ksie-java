package com.guardtime.container.verification;

import com.guardtime.container.packaging.Container;
import com.guardtime.container.util.Util;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.policy.VerificationPolicy;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.ContainerVerifierResult;
import com.guardtime.container.verification.rule.ContainerRule;
import com.guardtime.container.verification.rule.RuleTerminatingException;

import java.util.LinkedList;
import java.util.List;

/**
 * Helper class to verify {@link Container} based on a {@link VerificationPolicy}
 */
public class ContainerVerifier {

    private VerificationPolicy policy;

    public ContainerVerifier(VerificationPolicy policy) {
        Util.notNull(policy, "Verification policy");
        this.policy = policy;
    }

    /**
     * Verifies the {@link Container} based on the rules provided by the {@link VerificationPolicy}.
     *
     * @param container
     *         container to be verified
     * @return {@link ContainerVerifierResult} based on all {@link RuleVerificationResult} gathered during verification.
     */
    public ContainerVerifierResult verify(Container container) {
        ResultHolder holder = new ResultHolder();
        for(ContainerRule rule : policy.getContainerRules()) {
            try {
                rule.verify(holder, container);
            } catch (RuleTerminatingException e) {
                break; // TODO: Might as well step this outside the for cycle as we intend to break anyway. Would be a nice place to just log down the termination reason.
            }
        }
        return new ContainerVerifierResult(container, holder);
    }

}
