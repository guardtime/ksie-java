package com.guardtime.container.packaging.parsing;

import com.guardtime.ksi.util.Util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MemoryBasedParsingStoreFactory implements ParsingStoreFactory {

    @Override
    public ParsingStore build() throws ParsingStoreException {
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
            return Collections.unmodifiableSet(store.keySet());
        }

        @Override
        public InputStream get(String name) throws ParsingStoreException {
            checkExistence(name);
            return new ByteArrayInputStream(store.get(name));
        }

        @Override
        public ParsedStreamProvider getParsedStreamProvider(String name) throws ParsingStoreException {
            checkExistence(name);
            return new MemoryBasedParsedStreamProvider(name, this);
        }

        @Override
        public void close() throws Exception {
            this.store = new HashMap<>();
        }

        private void checkExistence(String name) throws ParsingStoreException {
            if (!store.containsKey(name)) {
                throw new ParsingStoreException("No value matching '" + name + "' in store!");
            }
        }
    }

    private class MemoryBasedParsedStreamProvider implements ParsedStreamProvider {

        private final String name;
        private final ParsingStore parsingStore;

        MemoryBasedParsedStreamProvider(String name, ParsingStore parsingStore) {
            this.name = name;
            this.parsingStore = parsingStore;
        }

        @Override
        public InputStream getNewStream() throws ParsingStoreException {
            return parsingStore.get(name);
        }

        @Override
        public void close() throws Exception {
            // remove from store?
        }
    }
}
