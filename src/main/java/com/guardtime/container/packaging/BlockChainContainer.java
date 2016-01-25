package com.guardtime.container.packaging;

import com.guardtime.container.BlockChainContainerException;

import java.io.OutputStream;

public interface BlockChainContainer {

    void writeTo(OutputStream output) throws BlockChainContainerException;

}
