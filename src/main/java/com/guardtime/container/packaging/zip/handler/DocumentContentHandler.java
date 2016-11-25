package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.packaging.parsing.ParsedStreamProvider;
import com.guardtime.container.packaging.parsing.ParsingStoreException;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactoryBuilder;
import com.guardtime.container.packaging.parsing.ParsingStore;

import java.io.InputStream;

/**
 * This content holders is used for documents inside the container.
 */
public class DocumentContentHandler extends ContentHandler<ParsedStreamProvider> {

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
        return name.equals(ZipContainerPackagingFactoryBuilder.MIME_TYPE_ENTRY_NAME);
    }

    @Override
    protected ParsedStreamProvider getEntry(String name) throws ContentParsingException {
        try {
            return parsingStore.getParsedStreamProvider(name);
        } catch (ParsingStoreException e) {
            throw new ContentParsingException("Failed to get document content", e);
        }
    }

}
