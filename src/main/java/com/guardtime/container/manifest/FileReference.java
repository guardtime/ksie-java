package com.guardtime.container.manifest;

import com.guardtime.ksi.hashing.DataHash;

import java.util.List;

/**
 * Basic reference used by manifests to refer to files or other manifests.
 */
public interface FileReference {

    String getUri();

    String getMimeType();

    /**
     * Returns list of {@link DataHash} of the referred file.
     */
    List<DataHash> getHashList();


}
