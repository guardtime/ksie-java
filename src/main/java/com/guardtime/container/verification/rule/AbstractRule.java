package com.guardtime.container.verification.rule;

import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.RuleVerificationResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.state.RuleState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

import static com.guardtime.container.verification.result.ResultHolder.findHighestPriorityResult;
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
        if (this.state == RuleState.IGNORE || !getDependencyRuleResult(resultHolder, verifiable).equals(OK)) return false;
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

    private VerificationResult getDependencyRuleResult(ResultHolder holder, V verifiable) {
        List<RuleVerificationResult> results = getFilteredResults(holder, verifiable);
        if (!results.isEmpty()) {
            return findHighestPriorityResult(results);
        }
        return OK;
    }

    // Sub classes override to provide correct filtering
    protected List<RuleVerificationResult> getFilteredResults(ResultHolder holder, V verifiable) {
        return Collections.emptyList();
    }
}
