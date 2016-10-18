package com.guardtime.container.verification.result;

import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;

import java.util.List;

/**
 * Encompasses all results from verifying a {@link Container} Provides easier access to overall result of verification.
 */
public class ContainerVerifierResult {
    private final VerificationResult aggregateResult;
    private final ResultHolder resultHolder;
    private final Container container;

    public ContainerVerifierResult(Container container, ResultHolder holder) {
        this.container = container;
        this.resultHolder = holder;
        this.aggregateResult = findHighestPriorityResult(resultHolder.getResults());
    }

    /**
     * Provides access to all the {@link RuleVerificationResult} gathered during verification.
     *
     * @return List of {@link RuleVerificationResult}
     */
    public List<RuleVerificationResult> getResults() {
        return resultHolder.getResults();
    }

    /**
     * Provides access to the overall {@link VerificationResult} of the verification.
     */
    public VerificationResult getVerificationResult() {
        return aggregateResult;
    }

    public SignatureResult getSignatureResult(SignatureContent content) {
        String path = content.getManifest().getRight().getSignatureReference().getUri();
        return resultHolder.getSignatureResult(path);
    }

    /**
     * Provides access to the {@link Container} which was verified.
     */
    public Container getContainer() {
        return container;
    }

    private VerificationResult findHighestPriorityResult(List<RuleVerificationResult> verificationResults) {
        VerificationResult returnable = VerificationResult.OK;
        for (RuleVerificationResult result : verificationResults) {
            VerificationResult verificationResult = result.getVerificationResult();
            if (verificationResult.isMoreImportantThan(returnable)) {
                returnable = verificationResult;
                if (VerificationResult.NOK.equals(returnable)) break; // No need to check once max failure level reached
            }
        }
        return returnable;
    }
}
