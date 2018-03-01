/*
 * Copyright 2013-2018 Guardtime, Inc.
 *
 * This file is part of the Guardtime client SDK.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES, CONDITIONS, OR OTHER LICENSES OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * "Guardtime" and "KSI" are trademarks or registered trademarks of
 * Guardtime, Inc., and no license to trademarks is granted; Guardtime
 * reserves and retains all trademark rights.
 */

package com.guardtime.envelope.verification.rule;

import com.guardtime.envelope.verification.result.ResultHolder;
import com.guardtime.envelope.verification.result.VerificationResult;
import com.guardtime.envelope.verification.result.VerificationResultFilter;
import com.guardtime.envelope.verification.rule.state.RuleState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.guardtime.envelope.verification.result.VerificationResult.NOK;
import static com.guardtime.envelope.verification.result.VerificationResult.OK;
import static com.guardtime.envelope.verification.result.VerificationResult.WARN;

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

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " {" +
                "name= \'" + getName() + '\'' +
                ", state= \'" + state + "\'}";
    }

}
