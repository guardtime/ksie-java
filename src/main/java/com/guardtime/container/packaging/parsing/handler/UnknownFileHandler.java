package com.guardtime.container.packaging.parsing.handler;

import com.guardtime.container.document.ParsedContainerDocument;
import com.guardtime.container.document.UnknownDocument;
import com.guardtime.container.packaging.parsing.store.ParsingStore;

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
        return new ParsedContainerDocument(parsingStore, name, "unknown", name);
    }

}
