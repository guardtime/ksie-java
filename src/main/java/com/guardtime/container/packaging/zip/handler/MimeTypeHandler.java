package com.guardtime.container.packaging.zip.handler;

import com.guardtime.container.packaging.BCCMimeType;
import com.guardtime.container.packaging.FileMimeType;

public class MimeTypeHandler extends ContentHandler<BCCMimeType> {

    private static final String MIMETYPE_FILE_NAME = "mimetype";

    @Override
    public boolean isSupported(String name) {
        return name.matches(MIMETYPE_FILE_NAME);
    }

    @Override
    public BCCMimeType get(String name) {
        return new FileMimeType(entries.get(name), name);
    }
}
