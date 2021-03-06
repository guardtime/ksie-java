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

import com.guardtime.ksi.util.Util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Keeps parsed data in memory.
 * <p>
 * NB! There is no protection against large data. Use with care!
 * </p>
 */
public class MemoryBasedParsingStore extends ParsingStore {

    private Map<UUID, byte[]> store = new HashMap<>();

    @Override
    void storeInternal(UUID uuid, InputStream stream) throws IOException {
        store.put(uuid, Util.toByteArray(stream));
    }

    @Override
    public InputStream getContent(UUID uuid) {
        byte[] bytes = store.get(uuid);
        if (bytes == null) {
            throw new IllegalStateException("Parsing store has lost content for ID '" + uuid.toString() + "'");
        }
        return new ByteArrayInputStream(bytes);
    }

    @Override
    protected void clearStore(UUID uuid) {
        store.remove(uuid);
    }

}
