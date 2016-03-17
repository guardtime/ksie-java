package com.guardtime.container.annotation;

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;

public interface ContainerAnnotation {

    ContainerAnnotationType getAnnotationType();

    String getDomain();

    InputStream getInputStream() throws IOException;

    DataHash getDataHash(HashAlgorithm algorithm) throws IOException;

}
