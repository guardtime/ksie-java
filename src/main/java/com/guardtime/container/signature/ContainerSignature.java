package com.guardtime.container.signature;


import java.io.OutputStream;

public interface ContainerSignature {

    void writeTo(OutputStream output);

}
