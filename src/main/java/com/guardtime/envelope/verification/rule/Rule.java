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

import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.verification.result.ResultHolder;
import com.guardtime.envelope.verification.result.RuleVerificationResult;

/**
 * Rule to be performed during verification of {@link Envelope} or its components.
 * @param <V> Verifiable object class.
 */
public interface Rule<V> {

    /**
     * Verifies {@link V} to produce a list of {@link RuleVerificationResult} which are added to the provided {@link
     * ResultHolder}. Depending on the implementation, there can be nested Rules used during verification.
     * @param verifiable object to be examined.
     * @param resultHolder that maintains all rule verification results.
     * @return True, unless the verification process is ignored.
     * @throws RuleTerminatingException when the verification process at this level can not be continued due to the situation
     * encountered during processing the rule. Passes any decision of continuing verification to higher level invoker of
     * verification.
     */
    boolean verify(ResultHolder resultHolder, V verifiable) throws RuleTerminatingException;

    /**
     * @return Unique string which can be used to identify the type of the rule.
     */
    String getName();

    /**
     * @return Error string of the rule that applies when the rule results in a not OK state.
     */
    String getErrorMessage();
}
