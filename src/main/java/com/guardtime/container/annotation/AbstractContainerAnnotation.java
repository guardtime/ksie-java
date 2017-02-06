package com.guardtime.container.annotation;

import com.guardtime.container.util.DataHashException;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

import static com.guardtime.container.util.Util.notNull;
import static com.guardtime.ksi.util.Util.toByteArray;

public abstract class AbstractContainerAnnotation implements ContainerAnnotation {
    protected static final Logger logger = LoggerFactory.getLogger(ContainerAnnotation.class);

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
    public DataHash getDataHash(HashAlgorithm algorithm) throws DataHashException {
        if (dataHash == null || !dataHash.getAlgorithm().equals(algorithm)) {
            try (InputStream inputStream = getInputStream()) {
                dataHash = Util.hash(inputStream, algorithm);
            } catch (IOException e) {
                throw new DataHashException("Failed to access data to generate hash.", e);
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
        return this.getClass().toString() +
                " {type= \'" + type.getContent() + "\'" +
                ", domain= \'" + domain + "\'" +
                ", content= \'" + getContent() + "\'}";
    }

    private String getContent() {
        try (InputStream inputStream = getInputStream()) {
            byte[] bytes = toByteArray(inputStream);
            return new String(bytes);
        } catch (IOException e) {
            logger.warn("Failed to get content of annotation.", e);
            return "";
        }
    }
}
