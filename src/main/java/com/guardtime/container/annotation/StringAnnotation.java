package com.guardtime.container.annotation;


import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import static com.guardtime.container.util.Util.hash;
import static com.guardtime.container.util.Util.notNull;

public class StringAnnotation implements ContainerAnnotation {

    private final String mimeType = "application/txt";
    private final String content;
    private final String domain;
    private final ContainerAnnotationType type;
    private DataHash dataHash;

    public StringAnnotation(ContainerAnnotationType type, String content, String domain) {
        notNull(content, "Type");
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
    public String getMimeType() {
        return mimeType;
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
    public DataHash getDataHash(HashAlgorithm algorithm) {
        if (dataHash == null || !dataHash.getAlgorithm().equals(algorithm)) {
            dataHash = hash(getInputStream(), algorithm);
        }
        return dataHash;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public String toString() {
        return "StringAnnotation {" +
                "mimeType='" + mimeType + "\'" +
                ", domain='" + domain + "\'}";
    }

}
