package com.guardtime.container.verification.result;

public class GenericVerificationResult implements RuleVerificationResult {
    private final VerificationResult result;
    private final String ruleName;
    private final String testedElement;
    private String ruleMessage;

    public GenericVerificationResult(VerificationResult result, String ruleName, String ruleMessage, String testedElement, Exception exception) {
        this(result, ruleName, ruleMessage, testedElement);
        this.ruleMessage = exception.getMessage();
    }


    public GenericVerificationResult(VerificationResult result, String ruleName, String ruleMessage, String testedElement) {
        this.result = result;
        this.testedElement = testedElement;
        this.ruleName = ruleName;
        this.ruleMessage = ruleMessage;
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
