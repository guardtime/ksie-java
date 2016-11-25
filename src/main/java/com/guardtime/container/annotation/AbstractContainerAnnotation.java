package com.guardtime.container.annotation;

import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;

import static com.guardtime.container.util.Util.notNull;
import static com.guardtime.ksi.util.Util.toByteArray;

public abstract class AbstractContainerAnnotation implements ContainerAnnotation {
    protected final String domain;
    protected final ContainerAnnotationType type;
    private DataHash dataHash;

    public AbstractContainerAnnotation(String domain, ContainerAnnotationType type) {
        notNull(domain, "Domain");
        notNull(type, "Annotation type");
        this.type = type;
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
    public DataHash getDataHash(HashAlgorithm algorithm) throws IOException {
        if (dataHash == null || !dataHash.getAlgorithm().equals(algorithm)) {
            try (InputStream inputStream = getInputStream()) {
                dataHash = Util.hash(inputStream, algorithm);
            }
        }
        return dataHash;
    }

    @Override
    public void close() throws Exception {
        // Nothing to close
    }

    @Override
    public String toString() {
        return "StringContainerAnnotation {" +
                "type=\'" + type.getContent() + "\'" +
                ", domain=\'" + domain + "\'" +
                ", content=\'" + getContent() + "\'}";
    }

    protected String getContent() {
        try (InputStream inputStream = getInputStream()) {
            byte[] bytes = toByteArray(inputStream);
            return new String(bytes);
        } catch (IOException e) {
            return "";
        }
    }
}
