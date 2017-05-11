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

import com.guardtime.ksi.util.Util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Keeps parsed data in memory.
 * NB! There is no defence against too large data. Use with care!
 */
public class MemoryBasedParsingStoreFactory implements ParsingStoreFactory {

    @Override
    public ParsingStore create() throws ParsingStoreException {
        return new MemoryBasedParsingStore();
    }

    private class MemoryBasedParsingStore implements ParsingStore {

        private Map<String, byte[]> store = new HashMap<>();

        @Override
        public void store(String name, InputStream stream) throws ParsingStoreException {
            try {
                store.put(name, Util.toByteArray(stream));
            } catch (IOException e) {
                throw new ParsingStoreException("Failed to access data in stream!", e);
            }
        }

        @Override
        public Set<String> getStoredNames() {
            return new HashSet<>(store.keySet());
        }

        @Override
        public InputStream get(String name) {
            if (!contains(name)) {
                return null;
            }
            return new ByteArrayInputStream(store.get(name));
        }

        @Override
        public boolean contains(String key) {
            return store.containsKey(key);
        }

        @Override
        public void remove(String key) {
            store.remove(key);
        }

        @Override
        public void close() throws Exception {
            this.store = new HashMap<>();
        }
    }

}
