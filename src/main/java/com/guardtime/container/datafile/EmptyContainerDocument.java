package com.guardtime.container.datafile;

import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;

public class EmptyContainerDocument implements ContainerDocument {
    private final String fileName;
    private final String mimeType;
    private final DataHash hash;

    public EmptyContainerDocument(String fileName, String mimeType, DataHash hash) {
        Util.notNull(fileName, "File name");
        Util.notNull(mimeType, "MIME type");
        Util.notNull(hash, "Data hash");
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.hash = hash;
    }

    @Override
    public String getFileName() {
        return null;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public DataHash getDataHash(HashAlgorithm algorithm) throws IOException {
        return hash;
    }

    @Override
    public boolean isWritable() {
        return false;
    }
}
