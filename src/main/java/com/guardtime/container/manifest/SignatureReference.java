package com.guardtime.container.manifest;

/**
 * Reference to a signature contained in a container.
 */
public interface SignatureReference {

    String getUri();

    /**
     * Returns MIME type of the signature.
     */
    String getType();

}
