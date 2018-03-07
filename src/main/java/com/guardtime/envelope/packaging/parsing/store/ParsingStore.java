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

package com.guardtime.envelope.packaging.parsing.store;

import com.guardtime.envelope.packaging.Envelope;

import java.io.InputStream;
import java.util.Set;

/**
 * Data store that is meant to keep data from parsed in {@link Envelope}.
 */
public interface ParsingStore extends AutoCloseable {

    /**
     * @param key the key to use to store the data read from the stream.
     * @param stream the {@link InputStream} from which the data will be stored.
     *
     * @throws ParsingStoreException when reading the stream fails.
     */
    void store(String key, InputStream stream) throws ParsingStoreException;

    Set<String> getStoredKeys();

    /**
     * @param key the key used to store the data of interest.
     * @return An {@link InputStream} of the data stored with the key.
     */
    InputStream get(String key);

    boolean contains(String key);

    void remove(String key);

    /**
     * Takes all the contents of <code>that</code> and adds it to this.
     * @param that ParsingStore containing the contents to be added.
     * @throws ParsingStoreException when adding the content fails.
     */
    void transferFrom(ParsingStore that) throws ParsingStoreException;

}
