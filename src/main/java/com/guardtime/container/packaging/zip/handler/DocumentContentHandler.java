package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.packaging.zip.ZipContainerPackagingFactoryBuilder;
import com.guardtime.container.packaging.zip.parsing.ParsingStore;

import java.io.InputStream;

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
        return name.equals(ZipContainerPackagingFactoryBuilder.MIME_TYPE_ENTRY_NAME);
    }

    @Override
    protected InputStream getEntry(String name) throws ContentParsingException {
        return fetchStreamFromEntries(name);
    }

}
