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

package com.guardtime.envelope.verification.rule.generic;

import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.verification.result.GenericVerificationResult;
import com.guardtime.envelope.verification.result.ResultHolder;
import com.guardtime.envelope.verification.result.VerificationResult;
import com.guardtime.envelope.verification.rule.AbstractRule;
import com.guardtime.envelope.verification.rule.RuleTerminatingException;
import com.guardtime.envelope.verification.rule.RuleType;
import com.guardtime.envelope.verification.rule.state.RuleStateProvider;

/**
 * Rule that verifies that there is a signature in the envelope for the given {@link SignatureContent}.
 * Will terminate verification upon non OK results.
 */
public class SignatureExistenceRule extends AbstractRule<SignatureContent> {

    private static final String NAME = RuleType.KSIE_VERIFY_SIGNATURE_EXISTS.getName();

    public SignatureExistenceRule(RuleStateProvider stateProvider) {
        super(stateProvider.getStateForRule(NAME));
    }

    @Override
    protected void verifyRule(ResultHolder holder, SignatureContent verifiable) throws RuleTerminatingException {
        String uri = verifiable.getManifest().getSignatureReference().getUri();
        EnvelopeSignature signature = verifiable.getEnvelopeSignature();
        if (signature == null || signature.getSignature() == null) {
            holder.addResult(
                    verifiable,
                    new GenericVerificationResult(VerificationResult.NOK, getName(), getErrorMessage(), uri)
            );
            throw new RuleTerminatingException("No signature present!");
        } else {
            holder.addResult(
                    verifiable,
                    new GenericVerificationResult(VerificationResult.OK, getName(), getErrorMessage(), uri)
            );
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getErrorMessage() {
        return "No signature in envelope for manifest!";
    }
}
