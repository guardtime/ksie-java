package com.guardtime.container.document;

import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.guardtime.container.util.Util.notNull;

/**
 * Document that is based on a {@link File}.
 */
public class FileContainerDocument implements ContainerDocument {

    private final File file;
    private final String mimeType;
    private final String fileName;
    private DataHash dataHash;

    public FileContainerDocument(File file, String mimeType) {
        this(file, mimeType, null);
    }

    public FileContainerDocument(File file, String mimeType, String fileName) {
        notNull(file, "File");
        notNull(mimeType, "MIME type");
        this.file = file;
        this.mimeType = mimeType;
        this.fileName = fileName == null ? file.getName() : fileName;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    @Override
    public DataHash getDataHash(HashAlgorithm algorithm) throws IOException {
        if (dataHash == null || !dataHash.getAlgorithm().equals(algorithm)) {
            dataHash = Util.hash(getInputStream(), algorithm);
        }
        return dataHash;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public String toString() {
        return "FileContainerDocument {type=File" +
                ", fileName=" + fileName +
                ", mimeType=" + mimeType+ "}";
    }
}
