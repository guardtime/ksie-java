package com.guardtime.container.manifest;


import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Container structure manifest containing references to documents contained in the container.
 */
public interface DocumentsManifest extends MultiHashElement {

    /**
     * Returns InputStream containing this manifest.
     * @throws IOException when the InputStream can't be created or accessed.
     */
    InputStream getInputStream() throws IOException;

    List<? extends FileReference> getDocumentReferences();

    /**
     * Returns {@link DataHash} created based on the same data available from {@link #getInputStream()} for given algorithm.
     * @param algorithm to be used for generating the hash.
     * @throws IOException when the hash input data can't be accessed.
     */
    DataHash getDataHash(HashAlgorithm algorithm) throws IOException;

}
