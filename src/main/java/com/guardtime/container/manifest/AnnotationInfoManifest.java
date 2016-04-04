package com.guardtime.container.manifest;

import com.guardtime.container.ContainerFileElement;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;

public interface AnnotationInfoManifest extends ContainerFileElement {

    AnnotationReference getAnnotationReference();

    FileReference getDataManifestReference();

    InputStream getInputStream() throws IOException;

    DataHash getDataHash(HashAlgorithm algorithm) throws IOException;

}
