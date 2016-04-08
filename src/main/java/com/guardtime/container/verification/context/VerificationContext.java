package com.guardtime.container.verification.context;

import com.guardtime.container.ContainerFileElement;
import com.guardtime.container.packaging.BlockChainContainer;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.RuleVerificationResult;

import java.util.List;

public interface VerificationContext {

    /**
     * Provides the {@link BlockChainContainer} to be verified as well as containing a list of results from any verification done on the
     * container.
     *
     * @return
     */
    BlockChainContainer getContainer();

    /**
     * Provides access to the list of results from rules performed on the context.
     *
     * @return
     */
    List<RuleVerificationResult> getResults();

    /**
     * Provides access to a sub list of results that were performed on obj.
     *
     * @param element
     * @return
     */
    List<RuleVerificationResult> getResultsFor(ContainerFileElement element);

    void addResults(List<RuleVerificationResult> verificationResults);
}
