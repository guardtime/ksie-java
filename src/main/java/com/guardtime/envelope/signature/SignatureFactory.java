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

import com.guardtime.envelope.extending.ExtendingPolicy;
import com.guardtime.ksi.hashing.DataHash;

import java.io.InputStream;

/**
 * Creates and reads signatures used in {@link com.guardtime.envelope.packaging.Envelope}.
 */
public interface SignatureFactory {

    /**
     * @param hash to be signed.
     *
     * @return The signature contained in an {@link EnvelopeSignature} implementation.
     * @throws SignatureException when creating the signature for the given hash fails.
     */
    EnvelopeSignature create(DataHash hash) throws SignatureException;

    /**
     * @param input stream from which the signature is to be read.
     *
     * @return The signature contained in an {@link EnvelopeSignature} implementation.
     * @throws SignatureException when reading the stream fails or constructing a signature from the read data fails.
     */
    EnvelopeSignature read(InputStream input) throws SignatureException;

    /**
     * Updates the {@link EnvelopeSignature} to extend its underlying signature to a trust anchor.
     *
     * @param envelopeSignature the signature to be extended.
     * @param extender the extending logic for the underlying signature inside {@link EnvelopeSignature}.
     *
     * @throws SignatureException when the extending fails for any reason.
     */
    void extend(EnvelopeSignature envelopeSignature, ExtendingPolicy extender) throws SignatureException;

    SignatureFactoryType getSignatureFactoryType();

}
