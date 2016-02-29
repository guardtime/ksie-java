package com.guardtime.container.packaging.zip.handler;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Helper class for reading specific type of zip file entry. To check if zip entry can be used by current handler use
 * the {@link ContentHandler#isSupported(String)} method.
 *
 * @param <T> type of the entry
 */
public abstract class ContentHandler<T> {

    protected Map<String, File> entries = new TreeMap<>();

    public abstract boolean isSupported(String name);

    public void add(String name, File file) {
        entries.put(name, file);
    }

    public abstract T get(String name);

    public Set<String> getNames() {
        return entries.keySet();
    }

}
