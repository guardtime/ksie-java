package com.guardtime.container.manifest;

import com.guardtime.ksi.hashing.DataHash;

import java.io.InputStream;

public interface SignatureManifest {

    DataHash getDataHash();

    InputStream getInputStream();

    String getUri();

}
