package com.guardtime.container.manifest;

import com.guardtime.container.util.DataHashException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;

/**
 * Container structure manifest containing references to {@link AnnotationsManifest}, {@link DocumentsManifest} and
 * {@link com.guardtime.container.signature.ContainerSignature} contained in the container. This is the root manifest of
 * container structure.
 */
public interface Manifest {

    /**
     * Returns {@link DataHash} created based on the same data available from {@link #getInputStream()} for given algorithm.
     * @param algorithm to be used for generating the hash.
     * @throws DataHashException when the hash input data can't be accessed.
     */
    DataHash getDataHash(HashAlgorithm algorithm) throws DataHashException;

    /**
     * Returns InputStream containing this manifest.
     * @throws IOException when the InputStream can't be created or accessed.
     */
    InputStream getInputStream() throws IOException;

    FileReference getDocumentsManifestReference();

    FileReference getAnnotationsManifestReference();

    SignatureReference getSignatureReference();

    ManifestFactoryType getManifestFactoryType();
}
