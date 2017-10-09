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

package com.guardtime.envelope.signature.ksi;

import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.signature.SignatureException;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.unisignature.KSISignature;

import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link EnvelopeSignature} implementation with {@link KSISignature} as the underlying signature.
 */
class KsiEnvelopeSignature implements EnvelopeSignature<KSISignature> {

    private KSISignature signature;

    KsiEnvelopeSignature(KSISignature signature) {
        this.signature = signature;
    }

    @Override
    public KSISignature getSignature() {
        return signature;
    }

    @Override
    public DataHash getSignedDataHash() {
        return signature.getInputHash();
    }

    @Override
    public boolean isExtended() {
        return signature.isExtended();
    }

    @Override
    public void writeTo(OutputStream output) throws IOException {
        try {
            signature.writeTo(output);
        } catch (KSIException e) {
            throw new IOException("Writing signature to output failed", e);
        }
    }

    void setExtendedSignature(KSISignature newSignature) throws SignatureException {
        if (!newSignature.isExtended() ||
                !newSignature.getInputHash().equals(this.signature.getInputHash()) ||
                !newSignature.getAggregationTime().equals(this.signature.getAggregationTime()) ||
                !newSignature.getIdentity().equals(this.signature.getIdentity())
                ) {
            throw new SignatureException("Provided signature is not an extended variant of the existing signature!");
        }
        this.signature = newSignature;
    }

    @Override
    public int compareTo(EnvelopeSignature<KSISignature> ksiSignatureEnvelopeSignature) {
        return this.signature.getAggregationTime().compareTo(ksiSignatureEnvelopeSignature.getSignature().getAggregationTime());
    }
}
