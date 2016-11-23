package com.guardtime.container.annotation;


import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static com.guardtime.container.util.Util.hash;
import static com.guardtime.container.util.Util.notNull;

/**
 * Annotation that is based on a String as the data source.
 */
public class StringContainerAnnotation implements ContainerAnnotation {

    private final String content;
    private final String domain;
    private final ContainerAnnotationType type;
    private DataHash dataHash;

    public StringContainerAnnotation(ContainerAnnotationType type, String content, String domain) {
        notNull(type, "Type");
        notNull(content, "Content");
        this.type = type;
        this.content = content;
        this.domain = domain;
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
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content.getBytes(Charset.forName("UTF-8")));
    }

    @Override
    public DataHash getDataHash(HashAlgorithm algorithm) throws IOException {
        if (dataHash == null || !dataHash.getAlgorithm().equals(algorithm)) {
            try (InputStream inputStream = getInputStream()) {
                dataHash = hash(inputStream, algorithm);
            }
        }
        return dataHash;
    }

    @Override
    public String toString() {
        return "StringContainerAnnotation {" +
                ", domain='" + domain + "\'}";
    }

    @Override
    public void close() {
        //Nothing to do here, no resources held.
    }
}
