package com.guardtime.container.packaging;

import java.io.IOException;
import java.io.OutputStream;

public interface BlockChainContainer {

    void writeTo(OutputStream output) throws IOException;

}
