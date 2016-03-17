package com.guardtime.container.manifest;

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;

public interface SignatureManifest {

    DataHash getDataHash(HashAlgorithm algorithm) throws IOException;

    InputStream getInputStream() throws IOException;

    FileReference getDataFilesManifestReference();

    FileReference getAnnotationsManifestReference();

    SignatureReference getSignatureReference();

}
