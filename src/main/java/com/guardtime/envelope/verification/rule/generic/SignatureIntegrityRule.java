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
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.verification.result.GenericVerificationResult;
import com.guardtime.envelope.verification.result.ResultHolder;
import com.guardtime.envelope.verification.result.SignatureResult;
import com.guardtime.envelope.verification.result.VerificationResult;
import com.guardtime.envelope.verification.rule.AbstractRule;
import com.guardtime.envelope.verification.rule.RuleTerminatingException;
import com.guardtime.envelope.verification.rule.RuleType;
import com.guardtime.envelope.verification.rule.signature.SignatureVerifier;
import com.guardtime.envelope.verification.rule.state.RuleStateProvider;

/**
 * Rule that verifies the {@link EnvelopeSignature} of a {@link SignatureContent} by using a {@link SignatureVerifier}
 * to verify the underlying signature.
 * Will terminate verification upon non OK results.
 */
public class SignatureIntegrityRule extends AbstractRule<SignatureContent> {
    private static final String NAME = RuleType.KSIE_VERIFY_SIGNATURE.getName();
    private final SignatureVerifier verifier;

    public SignatureIntegrityRule(RuleStateProvider stateProvider, SignatureVerifier verifier) {
        super(stateProvider.getStateForRule(NAME));
        this.verifier = verifier;
    }

    @Override
    protected void verifyRule(ResultHolder holder, SignatureContent verifiable) throws RuleTerminatingException {
        Manifest manifest = verifiable.getManifest();
        String signatureUri = manifest.getSignatureReference().getUri();
        EnvelopeSignature envelopeSignature = verifiable.getEnvelopeSignature();
        VerificationResult result = getFailureVerificationResult();
        try {
            if (!verifier.isSupported(envelopeSignature)) {
                throw new RuleTerminatingException("Unsupported signature type!");
            }
            SignatureResult signatureResult =
                    verifier.getSignatureVerificationResult(envelopeSignature.getSignature(), manifest);
            signatureResult = new WrappedSignatureResult(signatureResult, result);
            holder.addSignatureResult(verifiable, signatureResult);
            holder.addResult(
                    verifiable,
                    new GenericVerificationResult(
                            signatureResult.getSimplifiedResult(),
                            getName(),
                            getErrorMessage(),
                            signatureUri
                    )
            );
        } catch (RuleTerminatingException e) {
            LOGGER.info("Verifying signature failed!", e);
            holder.addResult(verifiable, new GenericVerificationResult(result, getName(), getErrorMessage(), signatureUri, e));
            throw e;
        }
    }

    public String getName() {
        return NAME;
    }

    public String getErrorMessage() {
        return "Signature is invalid.";
    }

    private class WrappedSignatureResult implements SignatureResult {
        private final SignatureResult original;
        private final VerificationResult simpleResult;

        WrappedSignatureResult(SignatureResult result, VerificationResult verificationResult) {
            this.original = result;
            this.simpleResult = original.getSimplifiedResult() == null ? verificationResult : original.getSimplifiedResult();
        }

        @Override
        public VerificationResult getSimplifiedResult() {
            return simpleResult;
        }

        @Override
        public Object getSignature() {
            return original.getSignature();
        }

        @Override
        public Object getFullResult() {
            return original.getFullResult();
        }
    }
}
