package com.guardtime.container.verification.context;

import com.guardtime.container.packaging.BlockChainContainer;
import com.guardtime.container.verification.result.VerificationResult;

import java.util.LinkedList;
import java.util.List;

public class SimpleVerificationContext implements VerificationContext {
    private final BlockChainContainer container;
    private final List<VerificationResult> results = new LinkedList<>();

    public SimpleVerificationContext(BlockChainContainer container) {
        this.container = container;
    }

    @Override
    public BlockChainContainer getContainer() {
        return container;
    }

    @Override
    public List<VerificationResult> getResults() {
        return results;
    }
}
