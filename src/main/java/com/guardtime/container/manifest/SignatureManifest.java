package com.guardtime.container.manifest;

import com.guardtime.container.BlockChainContainerException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.InputStream;

public interface SignatureManifest {

    DataHash getDataHash(HashAlgorithm algorithm) throws BlockChainContainerException;

    InputStream getInputStream() throws BlockChainContainerException;

    String getUri();

}
