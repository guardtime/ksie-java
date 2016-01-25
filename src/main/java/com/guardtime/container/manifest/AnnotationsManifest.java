package com.guardtime.container.manifest;


import com.guardtime.container.BlockChainContainerException;

import java.io.InputStream;

public interface AnnotationsManifest {

    InputStream getInputStream() throws BlockChainContainerException;

    String getUri();

}
