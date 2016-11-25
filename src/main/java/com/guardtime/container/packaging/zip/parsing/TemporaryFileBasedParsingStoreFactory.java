package com.guardtime.container.packaging.zip.parsing;

import com.guardtime.container.util.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
            return store.keySet();
        }

        @Override
        public InputStream get(String name) throws ParsingStoreException {
            checkClosed();
            try {
                File file = store.get(name);
                if(file == null) {
                    return null;
                }
                return Files.newInputStream(file.toPath());
            } catch (IOException e) {
                throw new ParsingStoreException("Failed to retrieve stream for element '" + name + "'", e);
            }
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

        private void checkClosed() throws ParsingStoreException {
            if (closed) {
                throw new ParsingStoreException("Can't access a closed store!");
            }
        }
    }
}
