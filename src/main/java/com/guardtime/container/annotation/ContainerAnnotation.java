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

package com.guardtime.container.annotation;

import com.guardtime.container.util.DataHashException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents annotations that can be used in container. Combines annotation data and annotation
 * meta-data into one object.
 */
public interface ContainerAnnotation extends AutoCloseable {

    ContainerAnnotationType getAnnotationType();

    String getDomain();

    /**
     * Returns {@link InputStream} containing the annotation data.
     * @throws IOException when there is a problem creating or accessing the InputStream.
     */
    InputStream getInputStream() throws IOException;

    /**
     * Returns {@link DataHash} of annotation data for given algorithm.
     * @throws DataHashException when there is a problem generating the hash.
     */
    DataHash getDataHash(HashAlgorithm algorithm) throws DataHashException;

}
