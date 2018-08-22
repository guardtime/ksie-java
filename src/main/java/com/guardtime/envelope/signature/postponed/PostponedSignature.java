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

package com.guardtime.envelope.signature.postponed;

import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.envelope.signature.SignatureException;
import com.guardtime.envelope.util.Util;
import com.guardtime.ksi.hashing.DataHash;

import java.io.IOException;
import java.io.OutputStream;


/**
 * {@link EnvelopeSignature} implementation with a delegated EnvelopeSignature as the underlying signature.
 * Acts as a placeholder until it is provided with a proper EnvelopeSignature. This allows for postponing signing.
 */
class PostponedSignature<T> implements EnvelopeSignature<T> {

    private final DataHash dataHash;
    private EnvelopeSignature<T> internalSignature = null;

    PostponedSignature(DataHash hash) {
        Util.notNull(hash, "Data hash");
        this.dataHash = hash;
    }

    /**
     * Delegates to real {@link EnvelopeSignature} if one is present.
     * Otherwise will write a placeholder string that can be later replaced with the real signature.
     */
    @Override
    public void writeTo(OutputStream output) throws IOException {
        if (internalSignature == null) {
            output.write(dataHash.getImprint());
        } else {
            internalSignature.writeTo(output);
        }
    }

    @Override
    public T getSignature() {
        if (internalSignature != null) {
            return internalSignature.getSignature();
        }
        return null;
    }

    @Override
    public DataHash getSignedDataHash() {
        if (internalSignature != null) {
            return internalSignature.getSignedDataHash();
        }
        return dataHash;
    }

    @Override
    public boolean isExtended() {
        return internalSignature != null && internalSignature.isExtended();
    }

    @Override
    public EnvelopeSignature<T> getCopy() {
        PostponedSignature<T> postponedSignature = new PostponedSignature<>(dataHash);
        if (postponedSignature.internalSignature != null) {
            postponedSignature.internalSignature = internalSignature.getCopy();
        }
        return postponedSignature;
    }

    @Override
    public int compareTo(EnvelopeSignature<T> o) {
        if (internalSignature != null) {
            return internalSignature.compareTo(o);
        }
        if (o.getSignature() == null) {
            // also an unsigned signature. compare hashes to maintain ordering
            return getSignedDataHash().toString().compareTo(o.getSignedDataHash().toString());
        }
        return 1;
    }

    /**
     * Assigns the real {@link EnvelopeSignature} to be used as delegate.
     *
     * @throws SignatureException When trying to replace signature of placeholder which has already been filled.
     * @throws IllegalArgumentException When the provided {@link EnvelopeSignature} has non-matching
     * {@link EnvelopeSignature#getSignedDataHash()} output.
     */
    void sign(EnvelopeSignature signature) throws SignatureException {
        if (internalSignature != null) {
            throw new SignatureException("Failed to assign signature to placeholder as it already has a signature!");
        }

        if (!signature.getSignedDataHash().equals(dataHash)) {
            throw new IllegalArgumentException("Provided signatures Data hash does not match!");
        }

        this.internalSignature = signature;
    }
}
