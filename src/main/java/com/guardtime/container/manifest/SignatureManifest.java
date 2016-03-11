package com.guardtime.container.manifest;

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;

//TODO rename to Manifest
public interface SignatureManifest {

    DataHash getDataHash(HashAlgorithm algorithm) throws IOException;

    InputStream getInputStream() throws IOException;

    FileReference getDataFilesReference();

    FileReference getAnnotationsManifestReference();

    SignatureReference getSignatureReference();

}
