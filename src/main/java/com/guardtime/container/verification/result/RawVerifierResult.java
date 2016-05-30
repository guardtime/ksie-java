package com.guardtime.container.verification.result;

import com.guardtime.container.packaging.Container;

import java.util.List;

/**
 * Encompasses all results from verifying a {@link Container} Provides easier access to overall result of verification.
 */
public class RawVerifierResult {
    private final VerificationResult aggregateResult;
    private final List<RuleVerificationResult> verificationResults;
    private final Container container;

    public RawVerifierResult(Container container, List<RuleVerificationResult> verificationResults) {
        this.container = container;
        this.verificationResults = verificationResults;
        this.aggregateResult = findHighestPriorityResult(verificationResults);
    }

    /**
     * Provides access to all the {@link RuleVerificationResult} gathered during verification.
     *
     * @return List of {@link RuleVerificationResult}
     */
    public List<RuleVerificationResult> getResults() {
        return verificationResults;
    }

    /**
     * Provides access to the overall {@link VerificationResult} of the verification.
     *
     * @return
     */
    public VerificationResult getVerificationResult() {
        return aggregateResult;
    }

    /**
     * Provides access to the {@link Container} which was verified.
     *
     * @return
     */
    public Container getContainer() {
        return container;
    }

    private VerificationResult findHighestPriorityResult(List<RuleVerificationResult> verificationResults) {
        VerificationResult returnable = VerificationResult.OK;
        for (RuleVerificationResult result : verificationResults) {
            VerificationResult verificationResult = result.getResultStatus();
            if (verificationResult.isMoreImportantThan(returnable)) {
                returnable = verificationResult;
                if (VerificationResult.NOK.equals(returnable)) break; // No need to check once max failure level reached
            }
        }
        return returnable;
    }
}
