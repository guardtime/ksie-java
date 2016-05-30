package com.guardtime.container.verification.result;

import com.guardtime.container.verification.rule.Rule;

public class GenericVerificationResult implements RuleVerificationResult {
    private final VerificationResult result;
    private final String ruleName;
    private final String ruleMessage;
    private final String testedElement;

    public GenericVerificationResult(VerificationResult result, Rule rule, String testedElement) {
        this.result = result;
        this.testedElement = testedElement;
        this.ruleName = rule.getName();
        this.ruleMessage = rule.getErrorMessage();
    }

    @Override
    public VerificationResult getResultStatus() {
        return result;
    }

    @Override
    public String getRuleName() {
        return ruleName;
    }

    @Override
    public String getRuleErrorMessage() {
        return ruleMessage;
    }


    @Override
    public String getTestedElementPath() {
        return testedElement;
    }

    @Override
    public boolean terminatesVerification() {
        return false;
    }
}
