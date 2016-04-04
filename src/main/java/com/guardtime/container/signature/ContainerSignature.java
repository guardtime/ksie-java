package com.guardtime.container.signature;


import com.guardtime.container.ContainerFileElement;

import java.io.IOException;
import java.io.OutputStream;

public interface ContainerSignature extends ContainerFileElement {

    void writeTo(OutputStream output) throws IOException;

}
