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

package com.guardtime.envelope.document;

import com.guardtime.envelope.EnvelopeElement;
import com.guardtime.envelope.util.DataHashException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Represents document data that is to be used in a envelope.
 */
public interface Document extends EnvelopeElement, AutoCloseable {

    String getFileName();

    String getMimeType();

    /**
     * Returns {@link InputStream} containing document.
     * @throws IOException when creating or accessing InputStream fails.
     */
    InputStream getInputStream() throws IOException;

    /**
     * Will return as many {@link DataHash}es as it can for provided {@link HashAlgorithm}s.
     * If no {@link DataHash}es can be generated then a {@link DataHashException} will be thrown.
     */
    List<DataHash> getDataHashList(List<HashAlgorithm> algorithmList) throws IOException, DataHashException;

    /**
     * Returns true for any document that's InputSteam can be accessed and data extracted from it.
     */
    boolean isWritable();
}

