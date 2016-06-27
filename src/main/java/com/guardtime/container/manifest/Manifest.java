package com.guardtime.container.manifest;

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;

public interface Manifest {

    DataHash getDataHash(HashAlgorithm algorithm) throws IOException;

    InputStream getInputStream() throws IOException;

    FileReference getDocumentsManifestReference();

    FileReference getAnnotationsManifestReference();

    SignatureReference getSignatureReference();

}
