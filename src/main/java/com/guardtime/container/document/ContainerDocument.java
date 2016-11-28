package com.guardtime.container.document;

import com.guardtime.container.manifest.MultiHashElement;
import com.guardtime.container.util.DataHashException;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Represents document data that is to be used in a container.
 */
public interface ContainerDocument extends MultiHashElement, AutoCloseable {

    String getFileName();

    String getMimeType();

    /**
     * Returns {@link InputStream} containing document.
     * @throws IOException when creating or accessing InputStream fails.
     */
    InputStream getInputStream() throws IOException;

    /**
     * Returns {@link DataHash} of document for given algorithm.
     * @throws IOException when there is a problem accessing the InputStream.
     * @throws DataHashException when the given algorithm can't be used for generating a hash.
     */
    DataHash getDataHash(HashAlgorithm algorithm) throws IOException, DataHashException;

    /**
     * Returns a List of {@link DataHash}es for provided {@link HashAlgorithm}s.
     */
    List<DataHash> getDataHashList(List<HashAlgorithm> algorithmList) throws IOException, DataHashException;

    /**
     * Returns true for any document that's InputSteam can be accessed and data extracted from it.
     */
    boolean isWritable();
}

