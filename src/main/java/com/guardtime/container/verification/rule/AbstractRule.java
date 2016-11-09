package com.guardtime.container.verification.rule;

import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.state.RuleState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRule<O> implements Rule<O> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(Rule.class);

    protected final RuleState state;

    protected AbstractRule(RuleState state) {
        this.state = state;
    }

    protected VerificationResult getFailureVerificationResult() {
        switch (state) {
            case WARN:
                return VerificationResult.WARN;
            case IGNORE:
                return VerificationResult.OK;
            default:
                return VerificationResult.NOK;
        }
    }

    @Override
    public Boolean verify(ResultHolder holder, O verifiable) throws RuleTerminatingException {
        if (this.state == RuleState.IGNORE) return false;
        verifyRule(holder, verifiable);
        return true;
    }

    protected abstract void verifyRule(ResultHolder holder, O verifiable) throws RuleTerminatingException;

    public String getName() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }
}
