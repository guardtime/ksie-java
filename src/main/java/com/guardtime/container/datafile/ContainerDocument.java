package com.guardtime.container.datafile;


import com.guardtime.container.ContainerFileElement;
import com.guardtime.container.util.DataHashException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;

public interface ContainerDocument extends ContainerFileElement {

    String getFileName();

    String getMimeType();

    InputStream getInputStream() throws IOException;

    DataHash getDataHash(HashAlgorithm algorithm) throws IOException, DataHashException;

    boolean isWritable();

}

