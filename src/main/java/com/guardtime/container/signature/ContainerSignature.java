package com.guardtime.container.signature;


import com.guardtime.container.ContainerFileElement;

import java.io.IOException;
import java.io.OutputStream;

/**
 * General interface for all possible signature implementations.
 */
public interface ContainerSignature extends ContainerFileElement {

    void writeTo(OutputStream output) throws IOException;

}
