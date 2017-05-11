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

package com.guardtime.container.packaging.parsing;

import java.io.InputStream;
import java.util.Set;

/**
 * Data store that is meant to keep data from parsed in {@link com.guardtime.container.packaging.Container}
 */
public interface ParsingStore extends AutoCloseable {

    /**
     * Adds data from {@param stream} into store with {@param key}
     * @throws ParsingStoreException when reading the stream fails.
     */
    void store(String key, InputStream stream) throws ParsingStoreException;

    Set<String> getStoredNames();

    /**
     * Produces an {@link InputStream} of the data stored with the {@param key}
     */
    InputStream get(String key);

    boolean contains(String key);

    void remove(String key);

}
