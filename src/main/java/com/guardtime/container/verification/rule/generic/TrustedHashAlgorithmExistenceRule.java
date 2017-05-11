/*
 * Copyright 2013-2017 Guardtime, Inc.
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

package com.guardtime.container.verification.rule.generic;

import com.guardtime.container.manifest.FileReference;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.container.verification.rule.state.RuleState;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import static com.guardtime.container.verification.result.VerificationResult.OK;

/**
 * Rule that checks whether there are any {@link DataHash}es with {@link HashAlgorithm} that have state of
 * {@link HashAlgorithm#status#NORMAL}
 */
public class TrustedHashAlgorithmExistenceRule extends AbstractRule<FileReference> {
    private final String ruleName;

    protected TrustedHashAlgorithmExistenceRule(RuleState state, String name) {
        super(state);
        this.ruleName = name;
    }

    @Override
    protected void verifyRule(ResultHolder holder, FileReference verifiable) throws RuleTerminatingException {
        VerificationResult verificationResult = getFailureVerificationResult();
        for (DataHash hash : verifiable.getHashList()) {
            if (hash.getAlgorithm().getStatus() == HashAlgorithm.Status.NORMAL) {
                verificationResult = OK;
            }
        }
        holder.addResult(new GenericVerificationResult(verificationResult, getName(), getErrorMessage(), verifiable.getUri()));
        if (!verificationResult.equals(OK)) {
            throw new RuleTerminatingException("No hashes with trusted hash algorithm found.");
        }
    }

    @Override
    public String getName() {
        return ruleName;
    }

    @Override
    public String getErrorMessage() {
        return "No trusted hash function";
    }
}
