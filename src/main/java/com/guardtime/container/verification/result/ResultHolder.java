package com.guardtime.container.verification.result;

import java.util.ArrayList;
import java.util.List;

public class ResultHolder {

    private final List<RuleVerificationResult> results;

    public ResultHolder() {
        this.results = new ArrayList<>();
    }

    public List<RuleVerificationResult> getResults() {
        return results;
    }

    public void addResult(RuleVerificationResult ruleVerificationResult) {
        results.add(ruleVerificationResult);
    }

    public void addResults(List<RuleVerificationResult> ruleVerificationResults) {
        results.addAll(ruleVerificationResults);
    }
}
