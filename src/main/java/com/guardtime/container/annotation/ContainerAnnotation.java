package com.guardtime.container.annotation;

import com.guardtime.container.ContainerFileElement;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;

public interface ContainerAnnotation extends ContainerFileElement {

    ContainerAnnotationType getAnnotationType();

    String getDomain();

    InputStream getInputStream() throws IOException;

    DataHash getDataHash(HashAlgorithm algorithm) throws IOException;

}
