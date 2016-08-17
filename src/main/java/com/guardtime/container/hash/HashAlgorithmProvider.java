package com.guardtime.container.hash;

import com.guardtime.ksi.hashing.HashAlgorithm;

import java.util.List;

/**
 * Helper that contains a selection of {@link HashAlgorithm}s that are to be used for producing {@link
 * com.guardtime.ksi.hashing.DataHash}es.
 */
public interface HashAlgorithmProvider {
    /**
     * Returns the main {@link HashAlgorithm} of the provider.
     */
    HashAlgorithm getMainHashAlgorithm();

    /**
     * Returns a {@link List} of all {@link HashAlgorithm}s of the provider.
     */
    List<HashAlgorithm> getHashAlgorithms();
}
