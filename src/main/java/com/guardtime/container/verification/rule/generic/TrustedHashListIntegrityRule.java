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
import com.guardtime.container.manifest.MultiHashElement;
import com.guardtime.container.util.DataHashException;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.result.GenericVerificationResult;
import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.AbstractRule;
import com.guardtime.container.verification.rule.RuleTerminatingException;
import com.guardtime.container.verification.rule.state.RuleState;
import com.guardtime.ksi.hashing.DataHash;

import java.util.List;

import static com.guardtime.container.verification.result.VerificationResult.OK;

/**
 * Rule that checks whether {@link DataHash}es in {@link FileReference} match those in {@link MultiHashElement}.
 */
public class TrustedHashListIntegrityRule extends AbstractRule<Pair<MultiHashElement, FileReference>> {
    private final String ruleName;

    protected TrustedHashListIntegrityRule(RuleState state, String name) {
        super(state);
        this.ruleName = name;
    }

    @Override
    protected void verifyRule(ResultHolder holder, Pair<MultiHashElement, FileReference> verifiable) throws RuleTerminatingException {
        FileReference reference = verifiable.getRight();
        VerificationResult verificationResult = getVerificationResult(reference.getHashList(), verifiable.getLeft());
        holder.addResult(new GenericVerificationResult(verificationResult, getName(), getErrorMessage(), reference.getUri()));

        if (!verificationResult.equals(OK)) {
            throw new RuleTerminatingException("Hash mismatch found.");
        }
    }

    private VerificationResult getVerificationResult(List<DataHash> hashList, MultiHashElement multiHashElement) {
        VerificationResult failureVerificationResult = getFailureVerificationResult();
        try {
            for (DataHash hash : hashList) {
                DataHash realHash = multiHashElement.getDataHash(hash.getAlgorithm());
                if (!realHash.equals(hash)) {
                    return failureVerificationResult;
                }
            }
        } catch (DataHashException e) {
            LOGGER.info("Failed to verify hash match.", e);
            return failureVerificationResult;
        }
        return OK;
    }

    @Override
    public String getName() {
        return ruleName;
    }

    @Override
    public String getErrorMessage() {
        return "Hash mismatch";
    }
}
