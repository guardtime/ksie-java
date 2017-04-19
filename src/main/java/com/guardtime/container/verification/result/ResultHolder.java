package com.guardtime.container.verification.result;

import com.guardtime.container.packaging.SignatureContent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ResultHolder {

    private final List<RuleVerificationResult> containerResults;
    private final Map<SignatureContent, List<RuleVerificationResult>> signatureContentResultsMap;
    private final Map<SignatureContent, List<SignatureResult>> signatureResultsMap = new HashMap<>();

    public ResultHolder() {
        this.containerResults = new ArrayList<>();
        this.signatureContentResultsMap = new HashMap<>();
    }

    public List<RuleVerificationResult> getResults() {
        List<RuleVerificationResult> allResults = new ArrayList<>();
        allResults.addAll(containerResults);
        for (List<RuleVerificationResult> results : signatureContentResultsMap.values()) {
            allResults.addAll(results);
        }
        return allResults;
    }

    public List<RuleVerificationResult> getResults(SignatureContent content) {
        List<RuleVerificationResult> contentVerificationResults = signatureContentResultsMap.get(content);
        if(contentVerificationResults == null) {
            return null;
        }
        List<RuleVerificationResult> ruleVerificationResults = new ArrayList<>(contentVerificationResults);
        ruleVerificationResults.addAll(containerResults); // Lets make sure that generic container rules are accessible as well
        return ruleVerificationResults;
    }

    public List<SignatureResult> getSignatureResults(SignatureContent content) {
        return signatureResultsMap.get(content);
    }

    public void addSignatureResult(SignatureContent content, SignatureResult result) {
        List<SignatureResult> signatureResults = signatureResultsMap.get(content);
        if (signatureResults == null) {
            signatureResults = new ArrayList<>();
            signatureResultsMap.put(content, signatureResults);
        }
        signatureResults.add(result);
    }

    public void addResult(RuleVerificationResult ruleVerificationResult) {
        containerResults.add(ruleVerificationResult);
    }

    public void addResults(List<RuleVerificationResult> ruleVerificationResults) {
        containerResults.addAll(ruleVerificationResults);
    }

    public void addResult(SignatureContent content, RuleVerificationResult ruleVerificationResult) {
        List<RuleVerificationResult> results = signatureContentResultsMap.get(content);
        if (results == null) {
            results = new ArrayList<>();
            signatureContentResultsMap.put(content, results);
        }
        results.add(ruleVerificationResult);
    }

    public void addResults(SignatureContent content, List<RuleVerificationResult> ruleVerificationResults) {
        List<RuleVerificationResult> results = signatureContentResultsMap.get(content);
        if (results == null) {
            results = new ArrayList<>();
            signatureContentResultsMap.put(content, results);
        }
        results.addAll(ruleVerificationResults);
    }

    public static VerificationResult findHighestPriorityResult(List<RuleVerificationResult> verificationResults) {
        VerificationResult returnable = VerificationResult.OK;
        if(verificationResults != null) {
            for (RuleVerificationResult result : verificationResults) {
                VerificationResult verificationResult = result.getVerificationResult();
                if (verificationResult.isMoreImportantThan(returnable)) {
                    returnable = verificationResult;
                    if (VerificationResult.NOK.equals(returnable)) break; // No need to check once max failure level reached
                }
            }
        }
        return returnable;
    }
}
