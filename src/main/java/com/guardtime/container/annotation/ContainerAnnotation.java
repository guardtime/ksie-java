package com.guardtime.container.annotation;

import com.guardtime.container.ContainerFileElement;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents annotations that can be used in container. Combines into one object both annotation data and annotation
 * meta-data like type and domain.
 */
public interface ContainerAnnotation extends ContainerFileElement {

    ContainerAnnotationType getAnnotationType();

    String getDomain();

    /**
     * Returns {@link InputStream} containing the annotation data.
     * @throws IOException when there is a problem creating or accessing the InputStream.
     */
    InputStream getInputStream() throws IOException;

    /**
     * Returns {@link DataHash} of annotation data for given algorithm.
     * @throws IOException when there is a problem accessing the InputStream.
     */
    DataHash getDataHash(HashAlgorithm algorithm) throws IOException;

}
