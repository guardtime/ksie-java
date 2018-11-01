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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Data store that is meant to keep data from parsed in {@link Envelope}.
 */
public abstract class ParsingStore {

    private Map<UUID, List<ParsingStoreReference>> references = new HashMap<>();

    public ParsingStoreReference store(InputStream stream) throws ParsingStoreException {
        return store(stream, null);
    }

    /**
     * Stores provided data stream with provided key and provides access to the stored data through an instance of
     * {@link ParsingStoreReference}
     *
     * @param stream the {@link InputStream} from which the data will be stored.
     * @param pathName optional name of stored file.
     * @throws ParsingStoreException when reading the stream fails.
     */
    public ParsingStoreReference store(InputStream stream, String pathName) throws ParsingStoreException {
        try {
            UUID uuid = UUID.randomUUID();
            storeInternal(uuid, stream);
            ParsingStoreReference reference = new ParsingStoreReference(uuid, this, pathName);
            updateReferences(uuid, reference);
            return reference;
        } catch (IOException e) {
            throw new ParsingStoreException("Failed to access data in stream!", e);
        }
    }

    public abstract InputStream getContent(UUID uuid);

    abstract void storeInternal(UUID uuid, InputStream inputStream) throws IOException;

    void updateReferences(UUID uuid, ParsingStoreReference parsingStoreReference) {
        List<ParsingStoreReference> currentReferences;
        if (references.containsKey(uuid)) {
            currentReferences = references.get(uuid);
        } else {
            currentReferences = new LinkedList<>();
            references.put(uuid, currentReferences);
        }
        currentReferences.add(parsingStoreReference);
    }

    void unregister(UUID uuid, ParsingStoreReference parsingStoreReference) {
        if (!references.containsKey(uuid)) {
            return;
        }
        List<ParsingStoreReference> currentReferences = references.get(uuid);
        currentReferences.remove(parsingStoreReference);
        if (currentReferences.isEmpty()) {
            clearStore(uuid);
            references.remove(uuid);
        }
    }

    protected abstract void clearStore(UUID uuid);

}
