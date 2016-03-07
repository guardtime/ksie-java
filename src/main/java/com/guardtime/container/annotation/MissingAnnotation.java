package com.guardtime.container.annotation;

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;

public class MissingAnnotation implements ContainerAnnotation {
    private final ContainerAnnotationType type;
    private final 
    @Override
    public ContainerAnnotationType getAnnotationType() {
        return null;
    }

    @Override
    public String getMimeType() {
        return null;
    }

    @Override
    public String getDomain() {
        return null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public DataHash getDataHash(HashAlgorithm algorithm) throws IOException {
        return null;
    }
}
