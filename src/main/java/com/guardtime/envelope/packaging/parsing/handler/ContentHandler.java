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

package com.guardtime.envelope.packaging.parsing.handler;

import com.guardtime.envelope.document.ParsedDocument;
import com.guardtime.envelope.document.UnknownDocument;
import com.guardtime.envelope.packaging.parsing.store.ParsingStore;
import com.guardtime.envelope.packaging.parsing.store.ParsingStoreException;

import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Helper class for reading specific type of zip file entry. To check if zip entry can be used by current handler use
 * the {@link ContentHandler#isSupported(String)} method.
 *
 * @param <T> type of the entry
 */
public abstract class ContentHandler<T> {

    protected final ParsingStore parsingStore;
    private Set<String> requestedEntries = new TreeSet<>();
    private Set<String> unrequestedEntries = new TreeSet<>();

    protected ContentHandler(ParsingStore store) {
        this.parsingStore = store;
    }

    public abstract boolean isSupported(String name);

    public void add(String name, InputStream stream) throws ParsingStoreException {
        parsingStore.store(name, stream);
        unrequestedEntries.add(name);
    }

    public T get(String name) throws ContentParsingException {
        T returnable = getEntry(name);
        markEntryRequested(name);
        return returnable;
    }

    protected abstract T getEntry(String name) throws ContentParsingException;

    public Set<String> getNames() {
        Set<String> names = new HashSet<>(unrequestedEntries);
        names.addAll(requestedEntries);
        return names;
    }

    public List<UnknownDocument> getUnrequestedFiles() throws ParsingStoreException {
        List<UnknownDocument> returnable = new LinkedList<>();
        for (String name : unrequestedEntries) {
            returnable.add(new ParsedDocument(parsingStore, name, "unknown", name));
        }
        return returnable;
    }

    protected boolean matchesSingleDirectory(String str, String dirName) {
        return str.matches("/?" + dirName + "/[^/]*");
    }

    protected boolean fileNameMatches(String str, String regex) {
        int startingIndex = str.startsWith("/") ? 1 : 0;
        return str.substring(startingIndex).matches(regex);
    }

    protected InputStream fetchStreamFromEntries(String name) throws ContentParsingException {
            InputStream inputStream = parsingStore.get(name);
            if (inputStream == null) throw new ContentParsingException("Failed to fetch file '" + name + "' from parsingStore.");
            return inputStream;
    }

    private void markEntryRequested(String name) {
        requestedEntries.add(name);
        unrequestedEntries.remove(name);
    }

    public void clearRequestedData() {
        for (String key : requestedEntries) {
            parsingStore.remove(key);
        }
    }
}
