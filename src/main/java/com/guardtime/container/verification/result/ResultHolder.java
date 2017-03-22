package com.guardtime.container.verification.result;

import com.guardtime.container.packaging.SignatureContent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultHolder {

    private final List<RuleVerificationResult> containerResults;
    private final Map<SignatureContent, List<RuleVerificationResult>> signatureContentResultsMap;
    private final Map<SignatureContent, List<SignatureResult>> signatureResultsMap = new HashMap<>();
    private List<RuleVerificationResult> activeResults;
    private List<SignatureResult> activeSignatureResults;

    public ResultHolder() {
        this.containerResults = new ArrayList<>();
        this.signatureContentResultsMap = new HashMap<>();
    }

    public List<RuleVerificationResult> getResults() {
        return containerResults;
    }

    public List<RuleVerificationResult> getResults(SignatureContent content) {
        return signatureContentResultsMap.get(content);
    }

    public List<SignatureResult> getSignatureResults(SignatureContent content) {
        return signatureResultsMap.get(content);
    }

    public void addSignatureResult(SignatureResult result) {
        activeSignatureResults.add(result);
    }

    public void addResult(RuleVerificationResult ruleVerificationResult) {
        activeResults.add(ruleVerificationResult);
    }

    public void addResults(List<RuleVerificationResult> ruleVerificationResults) {
        activeResults.addAll(ruleVerificationResults);
    }

    public void activateContainerResultsGathering() {
        activeResults = containerResults;
    }

    public void activateSignatureContentResultsGathering(SignatureContent content) {
        activeResults = signatureContentResultsMap.get(content);
        if(activeResults == null) {
            activeResults = signatureContentResultsMap.put(content, new ArrayList<RuleVerificationResult>());
        }
        activeSignatureResults = signatureResultsMap.get(content);
        if(activeSignatureResults == null) {
            activeSignatureResults = signatureResultsMap.put(content, new ArrayList<SignatureResult>());
        }
    }

    public static VerificationResult findHighestPriorityResult(List<RuleVerificationResult> verificationResults) {
        VerificationResult returnable = VerificationResult.OK;
        for (RuleVerificationResult result : verificationResults) {
            VerificationResult verificationResult = result.getVerificationResult();
            if (verificationResult.isMoreImportantThan(returnable)) {
                returnable = verificationResult;
                if (VerificationResult.NOK.equals(returnable)) break; // No need to check once max failure level reached
            }
        }
        return returnable;
    }
}
