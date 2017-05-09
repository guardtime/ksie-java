package com.guardtime.container.packaging.parsing.store;

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

    Set<String> getStoredKeys();

    /**
     * Produces an {@link InputStream} of the data stored with the {@param key}
     */
    InputStream get(String key);

    boolean contains(String key);

    void remove(String key);

    /**
     * Takes all the contents of {@param that} and adds it to this.
     * @param that
     */
    void absorb(ParsingStore that) throws ParsingStoreException;

}
