package com.guardtime.container.verification.result;

import com.guardtime.container.verification.context.VerificationContext;

import java.util.List;

public class VerifierResult {
    private final VerificationContext context;

    public VerifierResult(VerificationContext context) {
        this.context = context;
    }

    public List<RuleVerificationResult> getResults() {
        return context.getResults();
    }

    public RuleResult getVerificationResult() {
        RuleResult returnable = RuleResult.OK;
        for(RuleVerificationResult result : getResults()) {
            RuleResult ruleResult = result.getResult();
            if(ruleResult.isMoreImportant(returnable)) {
                returnable = ruleResult;
                if(RuleResult.NOK.equals(returnable)) break; // No need to check once max failure level reached
            }
        }
        return returnable;
    }
}
