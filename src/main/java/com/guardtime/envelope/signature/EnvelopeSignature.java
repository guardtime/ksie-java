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

package com.guardtime.envelope.signature;

import com.guardtime.ksi.hashing.DataHash;

import java.io.IOException;
import java.io.OutputStream;

/**
 * General interface for all possible signature implementations.
 * @param <O>   Class of the underlying signature.
 *
 * NB! compareTo should indicate which EnvelopeSignature was created first.
 */
public interface EnvelopeSignature<O> extends Comparable<EnvelopeSignature<O>> {

    /**
     * Write content of signature to output.
     * @param output stream to write signature to.
     * @throws IOException when the stream can't be written to.
     */
    void writeTo(OutputStream output) throws IOException;

    /**
     * Returns the underlying signature object.
     */
    O getSignature();

    /**
     * Returns the {@link DataHash} that is signed by the underlying signature.
     */
    DataHash getSignedDataHash();

    /**
     * Returns true if the underlying signature has been extended.
     */
    boolean isExtended();

}
