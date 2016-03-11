package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.util.Pair;

import java.io.File;
import java.util.*;

/**
 * Helper class for reading specific type of zip file entry. To check if zip entry can be used by current handler use
 * the {@link ContentHandler#isSupported(String)} method.
 *
 * @param <T> type of the entry
 */
public abstract class ContentHandler<T> {

    protected Map<String, File> entries = new TreeMap<>();
    private Set<String> unrequestedEntries = new TreeSet<>();

    public abstract boolean isSupported(String name);

    public void add(String name, File file) {
        entries.put(name, file);
        unrequestedEntries.add(name);
    }

    public T get(String name) {
        T returnable = getEntry(name);
        markEntryRequested(name);
        return returnable;
    }

    public abstract T getEntry(String name);

    public Set<String> getNames() {
        return entries.keySet();
    }

    public List<Pair<String, File>> getUnrequestedFiles() {
        List<Pair<String, File>> returnable = new LinkedList<>();
        for (String name : unrequestedEntries) {
            returnable.add(Pair.of(name, entries.get(name)));
        }
        return returnable;
    }

    protected boolean matchesSingleDirectory(String str, String dirName) {
        return str.matches("/?" + dirName + "/[^/]*");
    }

    protected boolean fileNameMatches(String str, String regex) {
        int startingIndex = str.contains("/") ? str.lastIndexOf("/") + 1 : 0;
        return str.substring(startingIndex).matches(regex);
    }

    private void markEntryRequested(String name) {
        unrequestedEntries.remove(name);
    }
}
