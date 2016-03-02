package com.guardtime.container.packaging;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileMimeType implements BCCMimeType {
    private final String uri;
    private final File mimeTypeFile;

    public FileMimeType(File mimeTypeFile, String uri) {
        this.mimeTypeFile = mimeTypeFile;
        this.uri = uri;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public InputStream getInputStream() {
        try {
            return new FileInputStream(mimeTypeFile);
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}
