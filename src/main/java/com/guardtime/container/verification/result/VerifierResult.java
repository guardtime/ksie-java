package com.guardtime.container.verification.result;

import com.guardtime.container.verification.context.VerificationContext;

import java.util.List;

public class VerifierResult {
    private final VerificationContext context;

    public VerifierResult(VerificationContext context) {
        this.context = context;
    }

    public List<VerificationResult> getResults() {
        return context.getResults();
    }

    public RuleResult getVerificationResult() {
        RuleResult returnable = RuleResult.OK;
        for(VerificationResult result : getResults()) {
            RuleResult ruleResult = result.getResult();
            if(ruleResult.isMoreImportant(returnable)) {
                returnable = ruleResult;
            }
        }
        return returnable;
    }
}
