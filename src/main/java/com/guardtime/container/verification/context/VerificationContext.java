package com.guardtime.container.verification.context;

import com.guardtime.container.packaging.BlockChainContainer;
import com.guardtime.container.verification.result.VerificationResult;

import java.util.List;

public interface VerificationContext {

    /**
     * Provides the container to be verified as well as containing a list of results from any verification done on the
     * container.
     *
     * @return
     */
    BlockChainContainer getContainer();

    /**
     * Provides access to the list of results from rules performed on the context.
     * @return
     */
    List<VerificationResult> getResults();
}
