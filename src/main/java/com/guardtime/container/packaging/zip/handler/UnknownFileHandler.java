package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.document.StreamContainerDocument;
import com.guardtime.container.document.UnknownDocument;
import com.guardtime.container.packaging.parsing.ParsingStore;

import java.io.IOException;
import java.io.InputStream;

/**
 * This content holders is used for any file. Use as the last place to catch any unfiltered files.
 */
public class UnknownFileHandler extends ContentHandler<UnknownDocument> {

    public UnknownFileHandler(ParsingStore store) {
        super(store);
    }

    @Override
    public boolean isSupported(String name) {
        return true;
    }

    @Override
    protected UnknownDocument getEntry(String name) throws ContentParsingException {
        try (InputStream inputStream = fetchStreamFromEntries(name)) {
            return new StreamContainerDocument(inputStream, "unknown", name);
        } catch (IOException e) {
            throw new ContentParsingException("Failed to parse unknown file.", e);
        }
    }

}
