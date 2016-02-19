package com.guardtime.container.manifest.reference;

import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.ksi.hashing.DataHash;

public interface AnnotationInfoManifestReference {
    String getUri();

    ContainerAnnotationType getType();

    DataHash getHash();
}
