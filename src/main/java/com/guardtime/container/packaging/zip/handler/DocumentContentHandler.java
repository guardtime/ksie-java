package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.packaging.parsing.ParsingStore;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactoryBuilder;

/**
 * This content holders is used for documents inside the container.
 */
public class DocumentContentHandler extends ContentHandler<ParsingStore> {

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
    protected ParsingStore getEntry(String name) throws ContentParsingException {
        if(parsingStore.get(name) == null) {
            throw new ContentParsingException("No data stored for entry '" + name + "'");
        }
        return parsingStore;
    }

}
