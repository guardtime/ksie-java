package com.guardtime.container.verification.rule;

import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
    public void verify(ResultHolder holder, O verifiable) throws RuleTerminatingException {
        if (this.state != RuleState.IGNORE) verifyRule(holder, verifiable);
    }

    protected abstract void verifyRule(ResultHolder holder, O verifiable) throws RuleTerminatingException;

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }
}
