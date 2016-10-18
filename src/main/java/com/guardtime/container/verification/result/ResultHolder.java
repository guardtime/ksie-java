package com.guardtime.container.verification.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultHolder {

    private final List<RuleVerificationResult> results;
    private Map<String, SignatureResult> signatureResults = new HashMap<>();

    public ResultHolder() {
        this.results = new ArrayList<>();
    }

    public List<RuleVerificationResult> getResults() {
        return results;
    }

    public Map<String, SignatureResult> getSignatureResults() {
        return signatureResults;
    }

    public SignatureResult getSignatureResult(String path) {
        return signatureResults.get(path);
    }

    public void setSignatureResult(String path, SignatureResult result) {
        signatureResults.put(path, result);
    }

    public void addResult(RuleVerificationResult ruleVerificationResult) {
        results.add(ruleVerificationResult);
    }

    public void addResults(List<RuleVerificationResult> ruleVerificationResults) {
        results.addAll(ruleVerificationResults);
    }
}
