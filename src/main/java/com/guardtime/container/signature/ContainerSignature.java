package com.guardtime.container.signature;


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
     * Returns true if passed in signatureClass is the same as O.
     */
    boolean isSupported(Class<?> signatureClass);

    /**
     * Returns the underlying signature object.
     */
    O getSignature();
}
