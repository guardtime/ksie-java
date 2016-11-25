package com.guardtime.container.packaging.zip.parsing;

import java.io.InputStream;
import java.util.Set;

public interface ParsingStore extends AutoCloseable {

    void store(String name, InputStream stream) throws ParsingStoreException;

    Set<String> getStoredNames();

    InputStream get(String name) throws ParsingStoreException;

//    void remove(String name) throws ParsingStoreException;
//
//    void remove(InputStream stream) throws ParsingStoreException;

}
