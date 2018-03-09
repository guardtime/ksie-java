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
 * Represents document data that is to be used in an envelope.
 */
public interface Document extends EnvelopeElement, AutoCloseable {

    String getFileName();

    String getMimeType();

    /**
     * @return {@link InputStream} containing document.
     * @throws IOException when creating or accessing InputStream fails.
     */
    InputStream getInputStream() throws IOException;

    /**
     * @param algorithmList List of all {@link HashAlgorithm}s to be used for generating {@link DataHash}es.
     *
     * @return As many {@link DataHash}es as it can for provided {@link HashAlgorithm}s.
     * @throws DataHashException, if no {@link DataHash} can be generated.
     */
    List<DataHash> getDataHashList(List<HashAlgorithm> algorithmList) throws DataHashException;

    /**
     * @return True for any document that's {@link InputStream} can be accessed and data extracted from it.
     */
    boolean isWritable();

}

