package com.guardtime.container.packaging.parsing;

import com.guardtime.container.util.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TemporaryFileBasedParsingStoreFactory implements ParsingStoreFactory {

    @Override
    public ParsingStore build() throws ParsingStoreException {
        try {
            return new TemporaryFileBasedParsingStore();
        } catch (IOException e) {
            throw new ParsingStoreException("Failed to create store!", e);
        }
    }

    private class TemporaryFileBasedParsingStore implements ParsingStore {

        private final Map<String, File> store = new HashMap<>();
        private final Path tempDir;
        private boolean closed;

        public TemporaryFileBasedParsingStore() throws IOException {
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
        public Set<String> getStoredNames() {
            return Collections.unmodifiableSet(store.keySet());
        }

        @Override
        public InputStream get(String name) throws ParsingStoreException {
            checkClosed();
            try {
                checkExistence(name);
                File file = store.get(name);
                return Files.newInputStream(file.toPath());
            } catch (IOException e) {
                throw new ParsingStoreException("Failed to retrieve stream for element '" + name + "'", e);
            }
        }

        @Override
        public ParsedStreamProvider getParsedStreamProvider(String name) throws ParsingStoreException {
            checkExistence(name);
            return new TemporatyFileBasedParsedStreamProvider(this, name);
        }

        @Override
        public void close() throws ParsingStoreException {
            try {
                Util.deleteFileOrDirectory(tempDir);
                this.closed = true;
            } catch (IOException e) {
                throw new ParsingStoreException("Failed to close all stored data!", e);
            }
        }

        private void checkExistence(String name) throws ParsingStoreException {
            if (!store.containsKey(name)) {
                throw new ParsingStoreException("No value matching '" + name + "' in store!");
            }
        }

        private void checkClosed() throws ParsingStoreException {
            if (closed) {
                throw new ParsingStoreException("Can't access a closed store!");
            }
        }
    }

    private class TemporatyFileBasedParsedStreamProvider implements ParsedStreamProvider {
        private final ParsingStore parsingStore;
        private final String key;

        public TemporatyFileBasedParsedStreamProvider(ParsingStore parsingStore, String name) {
            this.parsingStore = parsingStore;
            this.key = name;
        }

        @Override
        public InputStream getNewStream() throws ParsingStoreException {
            return parsingStore.get(key);
        }

        @Override
        public void close() throws Exception {
            // Nothing to do here at the moment
        }
    }
}
