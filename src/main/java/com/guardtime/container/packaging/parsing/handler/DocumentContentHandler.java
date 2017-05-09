package com.guardtime.container.packaging.parsing.handler;

import com.guardtime.container.packaging.parsing.store.ParsingStore;

import java.io.InputStream;

import static com.guardtime.container.packaging.MimeType.MIME_TYPE_ENTRY_NAME;

/**
 * This content holders is used for documents inside the container.
 */
public class DocumentContentHandler extends ContentHandler<InputStream> {

    public DocumentContentHandler(ParsingStore store) {
        super(store);
    }

    @Override
    public boolean isSupported(String name) {
        return !matchesMetaFolder(name) && !matchesMimeTypeFile(name);
    }

    private boolean matchesMetaFolder(String name) {
        return matchesSingleDirectory(name, "META-INF");
    }

    private boolean matchesMimeTypeFile(String name) {
        return name.equals(MIME_TYPE_ENTRY_NAME);
    }

    @Override
    protected InputStream getEntry(String name) throws ContentParsingException {
        if (!parsingStore.contains(name)) {
            throw new ContentParsingException("No data stored for entry '" + name + "'");
        }
        return parsingStore.get(name);
    }

}
