package com.guardtime.container.verification.result;

import com.guardtime.container.verification.rule.Rule;

public class GenericVerificationResult implements RuleVerificationResult {
    private final VerificationResult result;
    private final String ruleName;
    private final String testedElement;
    private String ruleMessage;

    public GenericVerificationResult(VerificationResult result, Rule rule, String testedElement, Exception exception) {
        this(result, rule, testedElement);
        this.ruleMessage = exception.getMessage();
    }


    public GenericVerificationResult(VerificationResult result, Rule rule, String testedElement) {
        this.result = result;
        this.testedElement = testedElement;
        this.ruleName = rule.getName();
        this.ruleMessage = rule.getErrorMessage();
    }

    @Override
    public VerificationResult getVerificationResult() {
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

}
