package com.guardtime.container.hash;

import com.guardtime.ksi.hashing.HashAlgorithm;

import java.util.List;

/**
 * Helper that contains a selection of {@link HashAlgorithm}s that are to be used for producing {@link
 * com.guardtime.ksi.hashing.DataHash}es.
 */
public interface HashAlgorithmProvider {

    /**
     * Returns a {@link List} of all {@link HashAlgorithm}s to be used for creating {@link
     * com.guardtime.ksi.hashing.DataHash}es for {@link com.guardtime.container.manifest.FileReference}.
     */
    List<HashAlgorithm> getFileReferenceHashAlgorithms();

    /**
     * Returns a {@link List} of all {@link HashAlgorithm}s to be used for creating {@link
     * com.guardtime.ksi.hashing.DataHash}es for {@link com.guardtime.container.manifest.FileReference} used
     * specifically for {@link com.guardtime.container.document.ContainerDocument}s.
     */
    List<HashAlgorithm> getDocumentReferenceHashAlgorithms();

    /**
     * Returns a {@link HashAlgorithm} to be used for creating {@link
     * com.guardtime.ksi.hashing.DataHash} for {@link com.guardtime.container.manifest.AnnotationDataReference}.
     */
    HashAlgorithm getAnnotationDataReferenceHashAlgorithm();

    /**
     * Returns a {@link HashAlgorithm} to be used when creating signature for the container.
     */
    HashAlgorithm getSignatureHashAlgorithm();
}
