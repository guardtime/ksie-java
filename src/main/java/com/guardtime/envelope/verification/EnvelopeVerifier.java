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

package com.guardtime.envelope.verification;

import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.util.Util;
import com.guardtime.envelope.verification.policy.VerificationPolicy;
import com.guardtime.envelope.verification.result.ResultHolder;
import com.guardtime.envelope.verification.result.RuleVerificationResult;
import com.guardtime.envelope.verification.rule.Rule;
import com.guardtime.envelope.verification.rule.RuleTerminatingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Helper class to verify {@link Envelope} based on a {@link VerificationPolicy}
 */
public class EnvelopeVerifier {
    private static final Logger logger = LoggerFactory.getLogger(EnvelopeVerifier.class);

    private VerificationPolicy policy;

    public EnvelopeVerifier(VerificationPolicy policy) {
        Util.notNull(policy, "Verification policy");
        this.policy = policy;
    }

    /**
     * Verifies the {@link Envelope} based on the rules provided by the {@link VerificationPolicy}.
     * @param envelope  envelope to be verified
     * @return {@link VerifiedEnvelope} based on all {@link RuleVerificationResult} gathered during verification.
     */
    public VerifiedEnvelope verify(Envelope envelope) {
        ResultHolder holder = new ResultHolder();
        try {
            verifyGeneralRules(envelope, holder);
            verifySignatureContents(envelope.getSignatureContents(), holder);
        } catch (RuleTerminatingException e) {
            logger.info("Envelope verification terminated! Reason: '{}'", e.getMessage());
        }
        return new VerifiedEnvelope(envelope, holder);
    }

    private void verifyGeneralRules(Envelope envelope, ResultHolder holder) throws RuleTerminatingException {
        for (Rule<Envelope> rule : policy.getEnvelopeRules()) {
            rule.verify(holder, envelope);
        }
    }

    private void verifySignatureContents(List<? extends SignatureContent> signatureContents, ResultHolder holder) {
        for (SignatureContent content : signatureContents) {
            try {
                for (Rule<SignatureContent> rule : policy.getSignatureContentRules()) {
                    rule.verify(holder, content);
                }
            } catch (RuleTerminatingException e) {
                logger.info("Signature content verification terminated! Reason: '{}'", e.getMessage());
            }
        }
    }

}
