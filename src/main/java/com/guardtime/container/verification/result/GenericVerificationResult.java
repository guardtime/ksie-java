package com.guardtime.container.verification.result;

import com.guardtime.container.ContainerFileElement;
import com.guardtime.container.verification.rule.Rule;

public class GenericVerificationResult implements RuleVerificationResult {
    private final RuleResult result;
    private final String ruleName;
    private final ContainerFileElement testedElement;

    public GenericVerificationResult(RuleResult result, Rule rule, ContainerFileElement testedElement) {
        this.result = result;
        this.testedElement = testedElement;
        this.ruleName = rule.getName();
    }

    @Override
    public RuleResult getResult() {
        return result;
    }

    @Override
    public String getRuleName() {
        return ruleName;
    }

    @Override
    public boolean terminatesVerification() {
        return false;
    }

    @Override
    public ContainerFileElement getTestedElement() {
        return testedElement;
    }
}
