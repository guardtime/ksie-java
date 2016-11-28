package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.document.ParsedContainerDocument;
import com.guardtime.container.document.UnknownDocument;
import com.guardtime.container.packaging.parsing.ParsingStore;
import com.guardtime.container.packaging.parsing.ParsingStoreException;

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

    protected final ParsingStore parsingStore;
    private Set<String> unrequestedEntries = new TreeSet<>();

    protected ContentHandler(ParsingStore store) {
        this.parsingStore = store;
    }

    public abstract boolean isSupported(String name);

    public void add(String name, InputStream stream) throws ParsingStoreException {
        parsingStore.store(name, stream);
        unrequestedEntries.add(name);
    }

    public T get(String name) throws ContentParsingException {
        T returnable = getEntry(name);
        markEntryRequested(name);
        return returnable;
    }

    protected abstract T getEntry(String name) throws ContentParsingException;

    public Set<String> getNames() {
        return parsingStore.getStoredNames();
    }

    public List<UnknownDocument> getUnrequestedFiles() throws ParsingStoreException {
        List<UnknownDocument> returnable = new LinkedList<>();
        for (String name : unrequestedEntries) {
            returnable.add(new ParsedContainerDocument(parsingStore, name, "unknown", name));
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
            InputStream inputStream = parsingStore.get(name);
            if (inputStream == null) throw new ContentParsingException("Failed to fetch file '" + name + "' from parsingStore.");
            return inputStream;
    }

    private void markEntryRequested(String name) {
        unrequestedEntries.remove(name);
    }
}
