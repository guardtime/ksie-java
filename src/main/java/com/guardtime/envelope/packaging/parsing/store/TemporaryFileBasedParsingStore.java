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
import com.guardtime.envelope.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Uses temporary files in system temp folder for maintaining data of parsed in {@link Envelope}.
 * <p>
 * NB! Does not provide protection against malicious file modification in temp folder. Use with care!
 * </p>
 */
public final class TemporaryFileBasedParsingStore extends ParsingStore {

    private static final Logger logger = LoggerFactory.getLogger(TemporaryFileBasedParsingStore.class);
    private static TemporaryFileBasedParsingStore instance;

    private final Map<UUID, File> store = new HashMap<>();
    private Path tempDir;

    private TemporaryFileBasedParsingStore() {
        // private!
    }

    public static ParsingStore getInstance() {
        if (instance == null) {
            instance = new TemporaryFileBasedParsingStore();
        }
        return instance;
    }

    public static boolean isInstanciated() {
        return instance != null;
    }

    @Override
    public ParsingStoreReference store(InputStream stream) throws ParsingStoreException {
        try {
            createTempDir();
            File tmpFile = Util.createTempFile(tempDir);
            Util.copyToTempFile(stream, tmpFile);
            UUID uuid = UUID.randomUUID();
            store.put(uuid, tmpFile);
            return addNewReference(uuid);
        } catch (IOException e) {
            throw new ParsingStoreException("Failed to store stream!", e);
        }
    }

    private void createTempDir() throws IOException {
        if (tempDir == null || !tempDir.toFile().exists()) {
            this.tempDir = Util.getTempDirectory();
        }
    }

    @Override
    public InputStream get(UUID uuid) {
        try {
            File file = store.get(uuid);
            return Files.newInputStream(file.toPath());
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Store has been corrupted! Expected to find file at '" + store.get(uuid).toPath() +
                            "' for key '" + uuid + "'", e
            );
        }
    }

    @Override
    protected void clearStore(UUID uuid) {
        File file = store.remove(uuid);
        if (file != null) {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException e) {
                logger.warn("Could not delete temporary file for key '{}'", uuid, e);
            }
        }
    }

}
