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

package com.guardtime.envelope.verification.rule.generic;

import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.util.DataHashException;
import com.guardtime.envelope.verification.result.GenericVerificationResult;
import com.guardtime.envelope.verification.result.ResultHolder;
import com.guardtime.envelope.verification.result.VerificationResult;
import com.guardtime.envelope.verification.rule.AbstractRule;
import com.guardtime.envelope.verification.rule.RuleTerminatingException;
import com.guardtime.envelope.verification.rule.RuleType;
import com.guardtime.envelope.verification.rule.state.RuleStateProvider;
import com.guardtime.ksi.hashing.DataHash;

/**
 * Rule to verify the input hash of root signature and {@link Manifest}.
 * Will terminate verification upon non OK results.
 */
public class SignatureSignsManifestRule extends AbstractRule<SignatureContent> {

    private static final String NAME = RuleType.KSIE_VERIFY_MANIFEST_HASH.getName();

    public SignatureSignsManifestRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
    }

    @Override
    protected void verifyRule(ResultHolder holder, SignatureContent verifiable) throws RuleTerminatingException {
        VerificationResult result = getFailureVerificationResult();
        Manifest manifest = verifiable.getManifest();
        try {
            DataHash signedHash = verifiable.getEnvelopeSignature().getSignedDataHash();
            DataHash realHash = manifest.getDataHash(signedHash.getAlgorithm());
            if (realHash.equals(signedHash)) {
                result = VerificationResult.OK;
            }
        } catch (DataHashException e) {
            throw new RuleTerminatingException("Failed to verify hash of manifest!", e);
        } finally {
            String manifestUri = verifiable.getManifest().getPath();
            holder.addResult(verifiable, new GenericVerificationResult(result, getName(), getErrorMessage(), manifestUri));
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getErrorMessage() {
        return "Manifest hash differs from the one signed!";
    }

}
