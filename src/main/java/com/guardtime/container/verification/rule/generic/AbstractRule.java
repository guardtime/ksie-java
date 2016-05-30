package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.RuleState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractRule<O> implements Rule<O>{
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

    protected boolean terminateVerification(List<RuleVerificationResult> verificationResults) {
        if(verificationResults.isEmpty()) return true;
        for (RuleVerificationResult result : verificationResults) {
            if (result.terminatesVerification() && !VerificationResult.OK.equals(result.getResultStatus())) {
                return true;
            }
        }
        return false;
    }

    protected boolean ignoreRule() {
        return this.state == RuleState.IGNORE;
    }

    @Override
    public List<RuleVerificationResult> verify(O verifiable){
        if(ignoreRule()) return new LinkedList<>();
        return verifyRule(verifiable);
    }

    protected abstract List<RuleVerificationResult> verifyRule(O verifiable);

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }
}
