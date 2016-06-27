package com.guardtime.container.manifest;

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;

public interface SingleAnnotationManifest {

    AnnotationDataReference getAnnotationReference();

    FileReference getDocumentsManifestReference();

    InputStream getInputStream() throws IOException;

    DataHash getDataHash(HashAlgorithm algorithm) throws IOException;

}
