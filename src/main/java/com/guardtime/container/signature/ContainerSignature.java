package com.guardtime.container.signature;

import com.guardtime.ksi.hashing.DataHash;

import java.io.IOException;
import java.io.OutputStream;

/**
 * General interface for all possible signature implementations.
 * @param <O>   Class of the underlying signature.
 */
public interface ContainerSignature<O> {

    /**
     * Write content of signature to output.
     * @param output stream to write signature to.
     * @throws IOException when the stream can't be written to.
     */
    void writeTo(OutputStream output) throws IOException;

    /**
     * Returns the underlying signature object.
     */
    O getSignature();

    /**
     * Returns the {@link DataHash} that is signed by the underlying signature.
     */
    DataHash getSignedDataHash();

    /**
     * Returns true if the underlying signature has been extended.
     */
    boolean isExtended();

}
