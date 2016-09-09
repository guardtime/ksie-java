package com.guardtime.container.manifest;

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;

/**
 * Container internal structure manifest that contains some meta-data and reference to annotation data.
 */
public interface SingleAnnotationManifest extends MultiHashElement {

    /**
     * Returns a reference pointing to annotation data in the container.
     */
    AnnotationDataReference getAnnotationReference();

    FileReference getDocumentsManifestReference();

    /**
     * Returns InputStream containing this manifest.
     * @throws IOException when the InputStream can't be created or accessed.
     */
    InputStream getInputStream() throws IOException;

    /**
     * Returns {@link DataHash} created based on the same data available from {@link #getInputStream()} for given algorithm.
     * @param algorithm to be used for generating the hash.
     * @throws IOException when the hash input data can't be accessed.
     */
    DataHash getDataHash(HashAlgorithm algorithm) throws IOException;

}
