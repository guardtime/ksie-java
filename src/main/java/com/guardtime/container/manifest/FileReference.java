package com.guardtime.container.manifest;

import com.guardtime.ksi.hashing.DataHash;

public interface FileReference {

    String getUri();

    String getMimeType();

    DataHash getHash();


}
