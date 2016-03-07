package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;

import java.io.File;

/**
 * This content holders is used for data files inside the container.
 */
public class DataFileContentHandler extends ContentHandler<File> {

    @Override
    public boolean isSupported(String name) {
        return !matchesMetaFolder(name) && !matchesMimeTypeFile(name);
    }

    private boolean matchesMetaFolder(String name) {
        return matchesSingleDirectory(name, "META-INF");
    }

    private boolean matchesMimeTypeFile(String name) {
        return name.equals(ZipContainerPackagingFactory.MIME_TYPE_ENTRY_NAME);
    }

    @Override
    public File get(String name) {
        return entries.get(name);
    }

}
