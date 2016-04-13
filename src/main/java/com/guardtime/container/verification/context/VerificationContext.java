package com.guardtime.container.verification.context;

import com.guardtime.container.ContainerFileElement;
import com.guardtime.container.packaging.BlockChainContainer;
import com.guardtime.container.verification.result.RuleVerificationResult;

import java.util.List;

/**
 * Helper class providing access to {@link BlockChainContainer} for verification and to keep track of {@link
 * RuleVerificationResult} that are produced as a result of the verification.
 */
public interface VerificationContext {

    /**
     * Provides the {@link BlockChainContainer} to be verified as well as containing a list of results from any
     * verification done on the container.
     *
     * @return
     */
    BlockChainContainer getContainer();

    /**
     * Provides access to the list of results from rules performed on the context.
     *
     * @return A new list containing all elements from the internally maintained list.
     */
    List<RuleVerificationResult> getResults();

    /**
     * Provides access to a sub list of results that were performed on obj.
     *
     * @param element
     *         The elements for which results should be found.
     * @return A new list containing all elements that refer to the input {@link ContainerFileElement}.
     */
    List<RuleVerificationResult> getResultsFor(ContainerFileElement element);

    void addResults(List<RuleVerificationResult> verificationResults);
}
