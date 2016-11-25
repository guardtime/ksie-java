package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.packaging.zip.parsing.ParsingStore;
import com.guardtime.container.packaging.zip.parsing.ParsingStoreException;
import com.guardtime.container.util.Pair;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Helper class for reading specific type of zip file entry. To check if zip entry can be used by current handler use
 * the {@link ContentHandler#isSupported(String)} method.
 *
 * @param <T> type of the entry
 */
public abstract class ContentHandler<T> {

    protected final ParsingStore entries;
    private Set<String> unrequestedEntries = new TreeSet<>();

    protected ContentHandler(ParsingStore entries) {
        this.entries = entries;
    }

    public abstract boolean isSupported(String name);

    public void add(String name, InputStream stream) throws ParsingStoreException {
        entries.store(name, stream);
        unrequestedEntries.add(name);
    }

    public T get(String name) throws ContentParsingException {
        T returnable = getEntry(name);
        markEntryRequested(name);
        return returnable;
    }

    protected abstract T getEntry(String name) throws ContentParsingException;

    public Set<String> getNames() {
        return entries.getStoredNames();
    }

    public List<Pair<String, InputStream>> getUnrequestedFiles() throws ParsingStoreException {
        List<Pair<String, InputStream>> returnable = new LinkedList<>();
        for (String name : unrequestedEntries) {
            returnable.add(Pair.of(name, entries.get(name)));
        }
        return returnable;
    }

    protected boolean matchesSingleDirectory(String str, String dirName) {
        return str.matches("/?" + dirName + "/[^/]*");
    }

    protected boolean fileNameMatches(String str, String regex) {
        int startingIndex = str.startsWith("/") ? 1 : 0;
        return str.substring(startingIndex).matches(regex);
    }

    protected InputStream fetchStreamFromEntries(String name) throws ContentParsingException {
        String exceptionMessage = "Failed to fetch file '" + name + "' from entries.";
        try {
            InputStream inputStream = entries.get(name);
            if (inputStream == null) throw new ContentParsingException(exceptionMessage);
            return inputStream;
        } catch (ParsingStoreException e) {
            throw new ContentParsingException(exceptionMessage, e);
        }
    }

    private void markEntryRequested(String name) {
        unrequestedEntries.remove(name);
    }
}
