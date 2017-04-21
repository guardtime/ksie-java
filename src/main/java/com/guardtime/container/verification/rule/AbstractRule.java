package com.guardtime.container.verification.rule;

import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.result.VerificationResultFilter;
import com.guardtime.container.verification.rule.state.RuleState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.guardtime.container.verification.result.VerificationResult.NOK;
import static com.guardtime.container.verification.result.VerificationResult.OK;
import static com.guardtime.container.verification.result.VerificationResult.WARN;

public abstract class AbstractRule<V> implements Rule<V> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(Rule.class);

    protected final RuleState state;

    protected AbstractRule(RuleState state) {
        this.state = state;
    }

    protected VerificationResult getFailureVerificationResult() {
        switch (state) {
            case WARN:
                return WARN;
            case IGNORE:
                return OK;
            default:
                return NOK;
        }
    }

    @Override
    public boolean verify(ResultHolder resultHolder, V verifiable) throws RuleTerminatingException {
        if (this.state == RuleState.IGNORE || dependencyRulesFailed(resultHolder, verifiable)) return false;
        verifyRule(resultHolder, verifiable);
        return true;
    }

    protected abstract void verifyRule(ResultHolder holder, V verifiable) throws RuleTerminatingException;

    public String getName() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    private boolean dependencyRulesFailed(ResultHolder resultHolder, V verifiable) {
        return !resultHolder.getFilteredAggregatedResult(getFilter(resultHolder, verifiable)).equals(OK);
    }

    // Sub classes override to provide correct filtering
    protected VerificationResultFilter getFilter(ResultHolder holder, V verifiable) {
        return ResultHolder.NONE;
    }
}
