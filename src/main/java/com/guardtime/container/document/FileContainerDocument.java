package com.guardtime.container.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.guardtime.container.util.Util.notNull;

/**
 * Document that is based on a {@link File}.
 */
public class FileContainerDocument extends AbstractContainerDocument {

    private final File file;

    public FileContainerDocument(File file, String mimeType) {
        this(file, mimeType, null);
    }

    public FileContainerDocument(File file, String mimeType, String fileName) {
        super(mimeType, getFileName(file, fileName));
        this.file = file;
    }

    private static String getFileName(File file, String fileName) {
        notNull(file, "File");
        return fileName == null ? file.getName() : fileName;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }
}
