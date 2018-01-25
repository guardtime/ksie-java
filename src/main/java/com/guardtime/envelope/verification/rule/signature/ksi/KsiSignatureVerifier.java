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

package com.guardtime.envelope.verification.rule.signature.ksi;

import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.util.DataHashException;
import com.guardtime.envelope.util.Util;
import com.guardtime.envelope.verification.result.SignatureResult;
import com.guardtime.envelope.verification.result.VerificationResult;
import com.guardtime.envelope.verification.rule.RuleTerminatingException;
import com.guardtime.envelope.verification.rule.signature.SignatureVerifier;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.publication.PublicationData;
import com.guardtime.ksi.unisignature.KSISignature;
import com.guardtime.ksi.unisignature.verifier.policies.Policy;

public class KsiSignatureVerifier implements SignatureVerifier<KSISignature> {

    protected final KSI ksi;
    protected final Policy policy;
    private PublicationData publication;

    public KsiSignatureVerifier(KSI ksi, Policy policy) {
        this(ksi, policy, null);
    }

    public KsiSignatureVerifier(KSI ksi, Policy policy, PublicationData publicationData) {
        Util.notNull(ksi, "KSI instance");
        this.ksi = ksi;
        this.policy = policy;
        this.publication = publicationData;
    }

    @Override
    public Boolean isSupported(EnvelopeSignature envelopeSignature) {
        return envelopeSignature.getSignature() instanceof KSISignature;
    }

    @Override
    public SignatureResult getSignatureVerificationResult(KSISignature signature, Manifest manifest)
            throws RuleTerminatingException {
        VerificationResult ruleResult = null;
        try {
            HashAlgorithm hashAlgorithm = signature.getInputHash().getAlgorithm();
            DataHash realHash = manifest.getDataHash(hashAlgorithm);
            com.guardtime.ksi.unisignature.verifier.VerificationResult ksiVerificationResult =
                    ksi.verify(signature, policy, realHash, publication);
            if (ksiVerificationResult.isOk()) {
                ruleResult = VerificationResult.OK;
            }
            return new KsiSignatureResult(ksiVerificationResult, ruleResult, signature);
        } catch (KSIException | DataHashException e) {
            throw new RuleTerminatingException("Failed to verify KSI signature.", e);
        }
    }

}
