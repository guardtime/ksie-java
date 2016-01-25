package com.guardtime.container.manifest;


import com.guardtime.container.BlockChainContainerException;

import java.io.InputStream;

public interface DataFilesManifest {

    String getUri();

    InputStream getInputStream() throws BlockChainContainerException;

}
