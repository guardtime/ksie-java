package com.guardtime.container.manifest;

import com.guardtime.ksi.hashing.DataHash;

/**
 * Basic reference used by manifests to refer to files or other manifests.
 */
public interface FileReference {

    String getUri();

    String getMimeType();

    /**
     * Returns {@link DataHash} of the referred file.
     */
    DataHash getHash();


}
