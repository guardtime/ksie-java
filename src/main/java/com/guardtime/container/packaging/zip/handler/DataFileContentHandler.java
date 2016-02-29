package com.guardtime.container.packaging.zip.handler;

import java.io.File;

/**
 * This content holders is used for data files inside the container.
 */
public class DataFileContentHandler extends ContentHandler<File> {

    @Override
    public boolean isSupported(String name) {
        return !name.startsWith("META-INF/") && !name.startsWith("/META-INF/");
    }

    @Override
    public File get(String name) {
        return entries.get(name);
    }

}
