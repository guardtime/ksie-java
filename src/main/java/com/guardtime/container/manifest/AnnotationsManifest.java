package com.guardtime.container.manifest;

import com.guardtime.container.ContainerFileElement;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface AnnotationsManifest extends ContainerFileElement {

    List<? extends FileReference> getSingleAnnotationManifestReferences();

    InputStream getInputStream() throws IOException;

    DataHash getDataHash(HashAlgorithm algorithm) throws IOException;

}
