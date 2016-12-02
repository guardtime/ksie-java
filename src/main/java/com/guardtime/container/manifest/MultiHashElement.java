package com.guardtime.container.manifest;

import com.guardtime.container.util.DataHashException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

public interface MultiHashElement {
    /**
     * Returns {@link DataHash} of MultiHashElement for given algorithm.
     * @throws DataHashException when the given algorithm can't be used for generating a hash or the data can't be accessed.
     */
    DataHash getDataHash(HashAlgorithm algorithm) throws DataHashException;
}
