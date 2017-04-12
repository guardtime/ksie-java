package com.guardtime.container.verification.result;

import com.guardtime.container.packaging.SignatureContent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ResultHolder {

    private final List<RuleVerificationResult> containerResults;
    private final List<RuleVerificationResult> allResults;
    private final Map<SignatureContent, List<RuleVerificationResult>> signatureContentResultsMap;
    private final Map<SignatureContent, List<SignatureResult>> signatureResultsMap = new HashMap<>();
    private List<RuleVerificationResult> activeResults;
    private List<SignatureResult> activeSignatureResults;

    public ResultHolder() {
        this.containerResults = new ArrayList<>();
        this.signatureContentResultsMap = new HashMap<>();
        this.allResults = new ArrayList<>();
        activateContainerResultsGathering();
    }

    public List<RuleVerificationResult> getResults() {
        return allResults;
    }

    public List<RuleVerificationResult> getResults(SignatureContent content) {
        List<RuleVerificationResult> ruleVerificationResults = new ArrayList<>(signatureContentResultsMap.get(content));
        ruleVerificationResults.addAll(containerResults); // Lets make sure that generic container rules are accessible as well
        return ruleVerificationResults;
    }

    public List<SignatureResult> getSignatureResults(SignatureContent content) {
        return signatureResultsMap.get(content);
    }

    public void addSignatureResult(SignatureResult result) {
        activeSignatureResults.add(result);
    }

    public void addResult(RuleVerificationResult ruleVerificationResult) {
        activeResults.add(ruleVerificationResult);
        allResults.add(ruleVerificationResult);
    }

    public void addResults(List<RuleVerificationResult> ruleVerificationResults) {
        activeResults.addAll(ruleVerificationResults);
        allResults.addAll(ruleVerificationResults);
    }

    public void activateContainerResultsGathering() {
        activeResults = containerResults;
    }

    public void activateSignatureContentResultsGathering(SignatureContent content) {
        if(!signatureContentResultsMap.containsKey(content)) {
            signatureContentResultsMap.put(content, new ArrayList<RuleVerificationResult>());
        }
        activeResults = signatureContentResultsMap.get(content);

        if(!signatureResultsMap.containsKey(content)) {
            signatureResultsMap.put(content, new ArrayList<SignatureResult>());
        }
        activeSignatureResults = signatureResultsMap.get(content);
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
