package com.guardtime.container.packaging.zip.handler;

import java.io.File;

/**
 * This content holders is used for any file. Use as the last place to catch any unfiltered files.
 */
public class UnknownFileHandler extends ContentHandler<File> {

    @Override
    public boolean isSupported(String name) {
        return true;
    }

    @Override
    protected File getEntry(String name) {
        return entries.get(name);
    }

}
