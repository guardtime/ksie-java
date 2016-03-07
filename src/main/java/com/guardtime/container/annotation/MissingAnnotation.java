package com.guardtime.container.annotation;

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;

public class MissingAnnotation implements ContainerAnnotation {
    private final ContainerAnnotationType type;
    private final String domain;
    private final DataHash hash;

    public MissingAnnotation(ContainerAnnotationType type, String domain, DataHash hash) {
        this.type = type;
        this.domain = domain;
        this.hash = hash;
    }

    @Override
    public ContainerAnnotationType getAnnotationType() {
        return type;
    }

    @Override
    public String getMimeType() {
        return null;
    }

    @Override
    public String getDomain() {
        return domain;
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
    public boolean writable() {
        return false;
    }
}
