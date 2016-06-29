package com.guardtime.container.manifest;

import com.guardtime.ksi.hashing.DataHash;

/**
 * Manifest internal reference used for referring to annotation data in container.
 */
public interface AnnotationDataReference {

    /**
     * Returns String representation of path to the annotation data in container.
     */
    String getUri();

    String getDomain();

    /**
     * Returns Hash of the annotation data.
     */
    DataHash getHash();

}
