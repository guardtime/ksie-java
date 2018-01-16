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

package com.guardtime.envelope.packaging.parsing.store;

import com.guardtime.envelope.packaging.Envelope;
import com.guardtime.envelope.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Uses temporary files in system temp folder for maintaining data of parsed in {@link Envelope}
 * NB! Does not provide protection against malicious file modification in temp folder. Use with care!
 */
public class TemporaryFileBasedParsingStoreFactory implements ParsingStoreFactory {

    private static final Logger logger = LoggerFactory.getLogger(TemporaryFileBasedParsingStore.class);

    @Override
    public ParsingStore create() throws ParsingStoreException {
        try {
            return new TemporaryFileBasedParsingStore();
        } catch (IOException e) {
            throw new ParsingStoreException("Failed to allocate store!", e);
        }
    }

    private static class TemporaryFileBasedParsingStore implements ParsingStore {

        private final Map<String, File> store = new HashMap<>();
        private final Path tempDir;
        private boolean closed;

        TemporaryFileBasedParsingStore() throws IOException {
            this.tempDir = Util.getTempDirectory();
        }

        @Override
        public void store(String name, InputStream stream) throws ParsingStoreException {
            checkClosed();
            try {
                File tmpFile = Util.createTempFile(tempDir);
                Util.copyToTempFile(stream, tmpFile);
                store.put(name, tmpFile);
            } catch (IOException e) {
                throw new ParsingStoreException("Failed to store stream!", e);
            }
        }

        @Override
        public Set<String> getStoredKeys() {
            return new HashSet<>(store.keySet());
        }

        @Override
        public InputStream get(String name) {
            checkClosed();
            if (!contains(name)) {
                return null;
            }
            try {
                File file = store.get(name);
                return Files.newInputStream(file.toPath());
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Store has been corrupted! Expected to find file at '" + store.get(name).toPath() +
                                "' for key '" + name + "'", e
                );
            }
        }

        @Override
        public boolean contains(String key) {
            return store.containsKey(key);
        }

        @Override
        public void remove(String key) {
            File f = store.remove(key);
            if (f != null) {
                try {
                    Files.deleteIfExists(f.toPath());
                } catch (IOException e) {
                    logger.warn("Could not delete temporary file for key '{}'", key, e);
                }
            }
        }

        @Override
        public void transferFrom(ParsingStore that) throws ParsingStoreException {
            for (String key : that.getStoredKeys()) {
                try (InputStream stream = that.get(key)) {
                    store(key, stream);
                    that.remove(key);
                } catch (IOException e) {
                    throw new ParsingStoreException("Failed to close origin stream.", e);
                }
            }
        }

        @Override
        public void close() throws ParsingStoreException {
            try {
                for (File f : store.values()) {
                    Files.deleteIfExists(f.toPath());
                }
                Util.deleteFileOrDirectory(tempDir);
                store.clear();
                this.closed = true;
            } catch (IOException e) {
                throw new ParsingStoreException("Failed to clean up all stored data!", e);
            }
        }

        private void checkClosed() {
            if (closed) {
                throw new IllegalStateException("Can't access a closed store!");
            }
        }
    }
}
