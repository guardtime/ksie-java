package com.guardtime.container.annotation;

import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Annotation that is based on File as the data source.
 */
public class FileContainerAnnotation implements ContainerAnnotation {

    private final File file;
    private final String domain;
    private final ContainerAnnotationType type;
    private DataHash dataHash;

    public FileContainerAnnotation(File file, String domain, ContainerAnnotationType type) {
        Util.notNull(file, "File");
        Util.notNull(domain, "Domain");
        Util.notNull(type, "Annotation type");
        this.file = file;
        this.domain = domain;
        this.type = type;
    }

    @Override
    public ContainerAnnotationType getAnnotationType() {
        return type;
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
    public void close() throws IOException {
        //Nothing to do here, we don't know where the input File is from so can't delete it.
    }
}
