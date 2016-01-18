package com.guardtime.container.packaging;

import java.io.IOException;
import java.io.OutputStream;

public interface BlockchainContainer {

    void writeTo(OutputStream output) throws IOException;

}
