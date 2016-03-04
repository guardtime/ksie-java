package com.guardtime.container.datafile;

import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;

public class DataHashContainerDocument implements ContainerDocument {
    private String mimeType;
    private DataHash hash;

    public DataHashContainerDocument(String mimeType, DataHash hash) {
        Util.notNull(mimeType, "MIME type");
        Util.notNull(hash, "Data hash");
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
