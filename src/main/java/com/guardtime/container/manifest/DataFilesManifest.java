package com.guardtime.container.manifest;


import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface DataFilesManifest {

    InputStream getInputStream() throws IOException;

    List<? extends FileReference> getDataFileReferences();

    DataHash getDataHash(HashAlgorithm algorithm) throws IOException;

}
