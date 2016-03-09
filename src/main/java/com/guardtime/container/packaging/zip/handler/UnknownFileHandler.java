package com.guardtime.container.packaging.zip.handler;

import java.io.File;

public class UnknownFileHandler extends ContentHandler<File> {

    @Override
    public boolean isSupported(String name) {
        return true;
    }

    @Override
    public File getEntry(String name) {
        return entries.get(name);
    }

}
