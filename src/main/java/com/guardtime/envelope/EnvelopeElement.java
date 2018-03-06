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

package com.guardtime.envelope;

import com.guardtime.envelope.util.DataHashException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;

/**
 * Umbrella interface for any expected element of an {@link com.guardtime.envelope.packaging.Envelope}.
 * Contains generic methods that all components of an {@link com.guardtime.envelope.packaging.Envelope} must implement.
 */
public interface EnvelopeElement {

    /**
     * @return The path of the element within the {@link com.guardtime.envelope.packaging.Envelope}.
     */
    String getPath();

    /**
     * @return {@link DataHash} for given algorithm based on object data.
     * @throws DataHashException when the given algorithm can't be used for generating a hash or the data can't be accessed.
     */
    DataHash getDataHash(HashAlgorithm algorithm) throws DataHashException;


    /**
     * @return {@link InputStream} containing data of object. Same data that is used for calculating {@link DataHash} by
     * {@link EnvelopeElement#getDataHash(HashAlgorithm)}.
     * @throws IOException When there are issues accessing object data.
     */
    InputStream getInputStream() throws IOException;
}
