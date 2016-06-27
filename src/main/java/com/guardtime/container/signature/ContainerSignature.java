package com.guardtime.container.signature;


import java.io.IOException;
import java.io.OutputStream;

/**
 * General interface for all possible signature implementations.
 */
public interface ContainerSignature {

    void writeTo(OutputStream output) throws IOException;

}
