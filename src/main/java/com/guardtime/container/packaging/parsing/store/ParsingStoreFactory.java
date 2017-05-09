package com.guardtime.container.packaging.parsing.store;

/**
 * Helper used to create new instances of {@link ParsingStore}
 */
public interface ParsingStoreFactory {
    ParsingStore create() throws ParsingStoreException;
}
