package com.guardtime.container.packaging.parsing;

import com.guardtime.ksi.util.Util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Keeps parsed data in memory.
 * NB! There is no defence against too large data. Use with care!
 */
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
        public InputStream get(String name) {
            byte[] bytes = store.get(name);
            if(bytes == null) {
                return null;
            }
            return new ByteArrayInputStream(bytes);
        }

        @Override
        public void close() throws Exception {
            this.store = new HashMap<>();
        }
    }

}
