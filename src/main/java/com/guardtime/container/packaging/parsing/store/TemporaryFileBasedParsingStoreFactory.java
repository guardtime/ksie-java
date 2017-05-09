package com.guardtime.container.packaging.parsing.store;

import com.guardtime.container.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Uses temporary files in system temp folder for maintaining data of parsed in {@link com.guardtime.container.packaging.Container}
 * NB! Does not provide protection against malicious file modification in temp folder. Use with care!
 */
public class TemporaryFileBasedParsingStoreFactory implements ParsingStoreFactory {

    private static final Logger logger = LoggerFactory.getLogger(TemporaryFileBasedParsingStore.class);

    @Override
    public ParsingStore create() throws ParsingStoreException {
        try {
            return new TemporaryFileBasedParsingStore();
        } catch (IOException e) {
            throw new ParsingStoreException("Failed to allocate store!", e);
        }
    }

    private static class TemporaryFileBasedParsingStore implements ParsingStore {

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
        public Set<String> getStoredKeys() {
            return new HashSet<>(store.keySet());
        }

        @Override
        public InputStream get(String name) {
            checkClosed();
            if (!contains(name)) {
                return null;
            }
            try {
                File file = store.get(name);
                return Files.newInputStream(file.toPath());
            } catch (IOException e) {
                throw new IllegalStateException("Store has been corrupted! Expected to find file at '" + store.get(name).toPath() + "' for key '" + name + "'", e);
            }
        }

        @Override
        public boolean contains(String key) {
            return store.containsKey(key);
        }

        @Override
        public void remove(String key) {
            File f = store.remove(key);
            if (f != null) {
                try {
                    Files.deleteIfExists(f.toPath());
                } catch (IOException e) {
                    logger.warn("Could not delete temporary file for key '{}'", key, e);
                }
            }
        }

        @Override
        public void absorb(ParsingStore that) throws ParsingStoreException {
            for(String key : that.getStoredKeys()) {
                store(key, that.get(key));
                that.remove(key);
            }
        }

        @Override
        public void close() throws ParsingStoreException {
            try {
                for (File f : store.values()) {
                    Files.deleteIfExists(f.toPath());
                }
                Util.deleteFileOrDirectory(tempDir);
                store.clear();
                this.closed = true;
            } catch (IOException e) {
                throw new ParsingStoreException("Failed to clean up all stored data!", e);
            }
        }

        private void checkClosed() {
            if (closed) {
                throw new IllegalStateException("Can't access a closed store!");
            }
        }
    }
}
