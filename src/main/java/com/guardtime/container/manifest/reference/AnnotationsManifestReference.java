package com.guardtime.container.manifest.reference;

import com.guardtime.ksi.hashing.DataHash;

public interface AnnotationsManifestReference {
    String getUri();

    String getMimeType();

    DataHash getHash();
}
