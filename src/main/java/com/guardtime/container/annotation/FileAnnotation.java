package com.guardtime.container.annotation;

import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileAnnotation implements ContainerAnnotation {

    private final String mimeType;
    private final File file;
    private final String domain;
    private final ContainerAnnotationType type;
    private DataHash dataHash;

    public FileAnnotation(File file, String mimeType, String domain, ContainerAnnotationType type) {
        Util.notNull(file, "File");
        Util.notNull(mimeType, "MIME type");
        Util.notNull(domain, "Domain");
        Util.notNull(type, "Container type");
        this.mimeType = mimeType;
        this.file = file;
        this.domain = domain;
        this.type = type;
    }

    @Override
    public ContainerAnnotationType getAnnotationType() {
        return type;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public String getDomain() {
        return domain;
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
    public boolean writable() {
        return true;
    }

}
