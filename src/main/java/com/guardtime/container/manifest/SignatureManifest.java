package com.guardtime.container.manifest;

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.InputStream;

public interface SignatureManifest {

    DataHash getDataHash(HashAlgorithm algorithm);

    InputStream getInputStream();

    String getUri();

}
