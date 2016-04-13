package com.guardtime.container.verification.result;

import com.guardtime.container.packaging.BlockChainContainer;
import com.guardtime.container.verification.context.VerificationContext;

import java.util.List;

/**
 * Encompasses all results from verifying a {@link BlockChainContainer} Provides easier access to overall result of
 * verification.
 */
public class VerifierResult {
    private final VerificationContext context;
    private final RuleResult aggregateResult;

    public VerifierResult(VerificationContext context) {
        this.context = context;
        this.aggregateResult = findHighestPriorityResult(context);
    }

    /**
     * Provides access to all the {@link RuleVerificationResult} gathered during verification.
     *
     * @return List of {@link RuleVerificationResult}
     */
    public List<RuleVerificationResult> getResults() {
        return context.getResults();
    }

    /**
     * Provides access to the overall {@link RuleResult} of the verification.
     *
     * @return
     */
    public RuleResult getVerificationResult() {
        return aggregateResult;
    }

    /**
     * Provides access to the {@link BlockChainContainer} which was verified.
     *
     * @return
     */
    public BlockChainContainer getBlockChainContainer() {
        return context.getContainer();
    }

    private RuleResult findHighestPriorityResult(VerificationContext context) {
        RuleResult returnable = RuleResult.OK;
        for (RuleVerificationResult result : getResults()) {
            RuleResult ruleResult = result.getResult();
            if (ruleResult.isMoreImportantThan(returnable)) {
                returnable = ruleResult;
                if (RuleResult.NOK.equals(returnable)) break; // No need to check once max failure level reached
            }
        }
        return returnable;
    }
}
