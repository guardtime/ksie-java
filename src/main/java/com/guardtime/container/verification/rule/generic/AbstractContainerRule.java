package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.ContainerRule;
import com.guardtime.container.verification.rule.RuleState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractContainerRule implements ContainerRule {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ContainerRule.class);

    protected final RuleState state;

    protected AbstractContainerRule(RuleState state) {
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
    public String getName() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }
}
