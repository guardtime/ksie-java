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

package com.guardtime.envelope.extending;

import com.guardtime.envelope.manifest.Manifest;
import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.packaging.SignatureContent;
import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.signature.SignatureException;
import com.guardtime.envelope.signature.SignatureFactory;
import com.guardtime.envelope.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extending for all signatures in a envelope.
 */
public class EnvelopeSignatureExtender {
    private static final Logger logger = LoggerFactory.getLogger(EnvelopeSignatureExtender.class);
    private final SignatureFactory signatureFactory;
    private final ExtendingPolicy policy;

    public EnvelopeSignatureExtender(SignatureFactory signatureFactory, ExtendingPolicy policy) {
        Util.notNull(signatureFactory, "Signature factory");
        Util.notNull(policy, "Extending policy");
        this.signatureFactory = signatureFactory;
        this.policy = policy;
    }

    /**
     * Extends each signature in input envelope and returns an {@link ExtendedEnvelope}.
     * If a signature extending fails it is logged at INFO level and skipped.
     * @param envelope    Envelope to be extended.
     */
    public ExtendedEnvelope extend(Envelope envelope) {
        for (SignatureContent content : envelope.getSignatureContents()) {
            Manifest manifest = content.getManifest().getRight();
            String signatureUri = manifest.getSignatureReference().getUri();
            try {
                EnvelopeSignature envelopeSignature = content.getEnvelopeSignature();
                signatureFactory.extend(envelopeSignature, policy);

                if (!envelopeSignature.isExtended()) {
                    logger.warn("Extending signature '{}' resulted in a non-extended signature without exception!", signatureUri);
                }
            } catch (SignatureException e) {
                logger.warn("Failed to extend signature '{}' because: '{}'", signatureUri, e.getMessage());
            }
        }
        return new ExtendedEnvelope(envelope);
    }

}
