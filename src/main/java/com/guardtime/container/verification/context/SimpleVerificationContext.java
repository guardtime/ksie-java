package com.guardtime.container.verification.context;

import com.guardtime.container.packaging.BlockChainContainer;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.RuleVerificationResult;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SimpleVerificationContext implements VerificationContext {
    private final BlockChainContainer container;
    private Map<Object, List<RuleVerificationResult>> resultsMap = new HashMap<>();

    public SimpleVerificationContext(BlockChainContainer container) {
        this.container = container;
    }

    @Override
    public BlockChainContainer getContainer() {
        return container;
    }

    @Override
    public List<RuleVerificationResult> getResults() {
        List<RuleVerificationResult> returnable = new LinkedList<>();
        for (List<RuleVerificationResult> list : resultsMap.values()) {
            returnable.addAll(list);
        }
        return returnable;
    }

    @Override
    public List<RuleVerificationResult> getResultsFor(Object obj) {
        List<RuleVerificationResult> results = resultsMap.get(obj);
        return results == null ? null : new LinkedList<>(results);
    }

    @Override
    public void addResults(List<Pair<? extends Object, ? extends RuleVerificationResult>> verificationResults) {
        for (Pair<? extends Object, ? extends RuleVerificationResult> result : verificationResults) {
            Object testedObject = result.getLeft();
            List<RuleVerificationResult> priorResults = resultsMap.get(testedObject);
            if (priorResults == null) {
                resultsMap.put(testedObject, new LinkedList<RuleVerificationResult>());
                priorResults = resultsMap.get(testedObject);
            }
            priorResults.add(result.getRight());
        }
    }
}
