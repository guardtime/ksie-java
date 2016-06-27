package com.guardtime.container.signature;


import java.io.IOException;
import java.io.OutputStream;

/**
 * General interface for all possible signature implementations.
 * @param <O>   Class of the underlying signature.
 */
public interface ContainerSignature<O> {

    void writeTo(OutputStream output) throws IOException;

    /**
     * Returns true if passed in signatureClass is the same as O.
     */
    boolean supportsSignatureClass(Class<?> signatureClass);

    /**
     * Returns the underlying signature object.
     */
    O getSignature();
}
