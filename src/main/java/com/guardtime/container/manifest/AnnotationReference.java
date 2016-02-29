package com.guardtime.container.manifest;

import com.guardtime.ksi.hashing.DataHash;

public interface AnnotationReference {

    String getUri();

    String getDomain();

    DataHash getHash();

}
