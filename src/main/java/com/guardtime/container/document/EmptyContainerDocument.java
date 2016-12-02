package com.guardtime.container.document;

import com.guardtime.container.util.DataHashException;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a document in a container which doesn't store the document data in the container.
 */
public class EmptyContainerDocument extends AbstractContainerDocument {
    private final Map<HashAlgorithm, DataHash> dataHashMap;

    public EmptyContainerDocument(String fileName, String mimeType, List<DataHash> hashes) {
        super(mimeType, fileName);
        Util.notEmpty(hashes, "Data hash list");
        this.dataHashMap = mapHashes(hashes);
    }

    private Map<HashAlgorithm, DataHash> mapHashes(List<DataHash> hashList) {
        Map<HashAlgorithm, DataHash> map = new HashMap<>();
        for (DataHash hash : hashList) {
            map.put(hash.getAlgorithm(), hash);
        }
        return map;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public DataHash getDataHash(HashAlgorithm algorithm) throws DataHashException {
        if (!dataHashMap.containsKey(algorithm)) {
            throw new DataHashException("Could not find pre-generated hash for algorithm '" + algorithm.getName() + "'");
        }
        return dataHashMap.get(algorithm);
    }

    @Override
    public boolean isWritable() {
        return false;
    }

}
