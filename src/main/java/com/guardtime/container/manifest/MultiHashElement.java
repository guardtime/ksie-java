package com.guardtime.container.manifest;

import com.guardtime.container.util.DataHashException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;

public interface MultiHashElement {
    /**
     * Returns {@link DataHash} of MultiHashElement for given algorithm.
     * @throws IOException when there is a problem accessing the object data.
     * @throws DataHashException when the given algorithm can't be used for generating a hash.
     */
    DataHash getDataHash(HashAlgorithm algorithm) throws IOException, DataHashException;
}
