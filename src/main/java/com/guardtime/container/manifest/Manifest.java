package com.guardtime.container.manifest;

import com.guardtime.container.ContainerFileElement;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;

/**
 * Container structure manifest containing references to {@link AnnotationsManifest}, {@link DocumentsManifest} and
 * {@link com.guardtime.container.signature.ContainerSignature} contained in the container. This is the root manifest of
 * container structure.
 */
public interface Manifest extends ContainerFileElement {

    /**
     * Returns {@link DataHash} of the manifest created based on the same data available from {@link #getInputStream()}
     * for given algorithm.
     * @param algorithm to be used for generating the hash.
     * @throws IOException when the hash input data can't be accessed.
     */
    DataHash getDataHash(HashAlgorithm algorithm) throws IOException;

    /**
     * Returns InputStream containing this manifest.
     * @throws IOException when the InputStream can't be created or accessed.
     */
    InputStream getInputStream() throws IOException;

    FileReference getDocumentsManifestReference();

    FileReference getAnnotationsManifestReference();

    SignatureReference getSignatureReference();

}
