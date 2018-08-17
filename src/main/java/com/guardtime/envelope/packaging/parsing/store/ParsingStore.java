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

    /**
     * @param key the key to use to store the data read from the stream.
     * @param stream the {@link InputStream} from which the data will be stored.
     *
     * @throws ParsingStoreException when reading the stream fails.
     */
    public abstract ParsingStoreReference store(String key, InputStream stream) throws ParsingStoreException;

    public abstract InputStream get(UUID uuid);

    ParsingStoreReference addNewReference(UUID uuid, String name) {
        ParsingStoreReference ref = new ParsingStoreReference(uuid, name, this);
        updateReferences(uuid, ref);
        return ref;
    }

    protected void updateReferences(UUID uuid, ParsingStoreReference parsingStoreReference) {
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
