package com.guardtime.container.document;

import com.guardtime.container.hash.HashAlgorithmProvider;
import com.guardtime.container.util.DataHashException;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Represents a document in a container which doesn't store the document data in the container.
 */
public class EmptyContainerDocument implements ContainerDocument {
    private final String fileName;
    private final String mimeType;
    private final List<DataHash> hashList;

    public EmptyContainerDocument(String fileName, String mimeType, List<DataHash> hashes) {
        Util.notNull(fileName, "File name");
        Util.notNull(mimeType, "MIME type");
        Util.notEmpty(hashes, "Data hash list");
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.hashList = hashes;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public DataHash getDataHash(HashAlgorithm algorithm) throws IOException, DataHashException {
        for (DataHash hash : hashList) {
            if (hash.getAlgorithm().equals(algorithm)) {
                return hash;
            }
        }
        throw new DataHashException("Could not find pre-generated hash for algorithm '" + algorithm.getName() + "'");
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public List<DataHash> getDataHashList(HashAlgorithmProvider algorithmProvider) {
        return hashList;
    }
}
