package com.guardtime.container.signature;


import java.io.IOException;
import java.io.OutputStream;

/**
 * General interface for all possible signature implementations.
 */
public interface ContainerSignature {

    /**
     * Write content of signature to output.
     * @param output stream to write signature to.
     * @throws IOException when the stream can't be written to.
     */
    void writeTo(OutputStream output) throws IOException;

}
