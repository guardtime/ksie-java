package com.guardtime.container.packaging.parsing;

import java.io.InputStream;
import java.util.Set;

/**
 * Data store that is meant to keep data from parsed in {@link com.guardtime.container.packaging.Container}
 */
public interface ParsingStore extends AutoCloseable {

    /**
     * Adds data from {@param stream} into store with {@param key}
     * @throws ParsingStoreException when reading the stream fails.
     */
    void store(String key, InputStream stream) throws ParsingStoreException;

    Set<String> getStoredNames();

    /**
     * Produces an {@link InputStream} of the data stored with the {@param key}
     */
    InputStream get(String key);

}
