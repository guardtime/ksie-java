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

package com.guardtime.envelope.packaging.parsing;

import com.guardtime.envelope.document.DocumentFactory;
import com.guardtime.envelope.document.UnknownDocument;
import com.guardtime.envelope.packaging.parsing.store.ParsingStore;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreException;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreReference;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Keeps track of content passed to {@link ParsingStore} and allows for easier retrieval of such content.
 */
public class ParsingStoreSession {
    private final ParsingStore store;
    private final Map<String, ParsingStoreReference> references = new HashMap<>();
    private final Set<String> requestedKeys = new HashSet<>();
    private DocumentFactory documentFactory;

    public ParsingStoreSession(ParsingStore store) {
        this.store = store;
        this.documentFactory = new DocumentFactory(store);
    }

    public List<UnknownDocument> getUnrequestedFiles() {
        List<UnknownDocument> returnable = new ArrayList<>();
        List<String> keys = new ArrayList<>(references.keySet());
        keys.removeAll(requestedKeys);
        for (String key : keys) {
            returnable.add((UnknownDocument) documentFactory.create(getReference(key), "unknown", key));
        }
        return returnable;
    }

    public List<String> getStoredKeys() {
        return new ArrayList<>(references.keySet());
    }

    public boolean contains(String path) {
        return references.containsKey(path);
    }

    public ParsingStoreReference getReference(String path) {
        requestedKeys.add(path);
        return new ParsingStoreReference(references.get(path));
    }

    public void store(String name, InputStream input) throws ParsingStoreException {
        ParsingStoreReference ref = store.store(input, name);
        references.put(name, ref);
    }

    /**
     * Clear all {@link ParsingStoreReference}s created during storing. Should be called once all necessary references have been
     * used.
     */
    public void clear() {
        for (ParsingStoreReference reference: references.values()) {
            reference.unstore();
        }
    }

    public ParsingStore getParsingStore() {
        return store;
    }
}
