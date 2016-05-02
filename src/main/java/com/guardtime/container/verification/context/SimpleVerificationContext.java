package com.guardtime.container.verification.context;

import com.guardtime.container.ContainerFileElement;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.verification.result.RuleVerificationResult;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SimpleVerificationContext implements VerificationContext {
    private final Container container;
    private Map<ContainerFileElement, List<RuleVerificationResult>> resultsMap = new HashMap<>();

    public SimpleVerificationContext(Container container) {
        this.container = container;
    }

    @Override
    public Container getContainer() {
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
    public List<RuleVerificationResult> getResultsFor(ContainerFileElement element) {
        List<RuleVerificationResult> results = resultsMap.get(element);
        return results == null ? null : new LinkedList<>(results);
    }

    @Override
    public void addResults(List<RuleVerificationResult> verificationResults) {
        for (RuleVerificationResult result : verificationResults) {
            ContainerFileElement testedElement = result.getTestedElement();
            List<RuleVerificationResult> priorResults = resultsMap.get(testedElement);
            if (priorResults == null) {
                resultsMap.put(testedElement, new LinkedList<RuleVerificationResult>());
                priorResults = resultsMap.get(testedElement);
            }
            priorResults.add(result);
        }
    }
}
