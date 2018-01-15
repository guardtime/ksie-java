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

package com.guardtime.envelope.signature.postponed;

import com.guardtime.envelope.signature.EnvelopeSignature;
import com.guardtime.ksi.hashing.DataHash;

import java.io.IOException;
import java.io.OutputStream;


/**
 * {@link EnvelopeSignature} implementation with a delegated EnvelopeSignature as the underlying signature.
 * Acts as a placeholder until it is provided with a proper EnvelopeSignature. This allows for postponing signing.
 */
class PostponedSignature implements EnvelopeSignature {

    private final DataHash dataHash;
    private EnvelopeSignature internalSignature = null;

    PostponedSignature(DataHash hash) {
        this.dataHash = hash;
    }

    /**
     * Delegates to real {@link EnvelopeSignature} if one is present. Otherwise will write a placeholder string that can be later
     * replaced with real signature.
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
    public Object getSignature() {
        if (internalSignature != null) {
            return internalSignature.getSignature();
        }
        return null;
    }

    @Override
    public DataHash getSignedDataHash() {
        if (internalSignature == null) {
            return dataHash;
        }
        return internalSignature.getSignedDataHash();
    }

    @Override
    public boolean isExtended() {
        if (internalSignature == null) {
            return false;
        }
        return internalSignature.isExtended();
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }

    /**
     * Assigns the real {@link EnvelopeSignature} to be used as delegate.
     */
    void sign(EnvelopeSignature realSignature) {
        if (internalSignature != null) {
            throw new UnsupportedOperationException();
        }
        this.internalSignature = realSignature;
    }

}
