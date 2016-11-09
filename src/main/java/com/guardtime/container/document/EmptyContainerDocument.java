package com.guardtime.container.document;

import com.guardtime.container.util.DataHashException;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Represents a document in a container which doesn't store the document data in the container.
 */
public class EmptyContainerDocument implements ContainerDocument {
    private final String fileName;
    private final String mimeType;
    private final Map<HashAlgorithm, DataHash> dataHashMap;

    public EmptyContainerDocument(String fileName, String mimeType, List<DataHash> hashes) {
        Util.notNull(fileName, "File name");
        Util.notNull(mimeType, "MIME type");
        Util.notEmpty(hashes, "Data hash list");
        this.fileName = fileName;
        this.mimeType = mimeType;
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
        if (!dataHashMap.containsKey(algorithm)) {
            throw new DataHashException("Could not find pre-generated hash for algorithm '" + algorithm.getName() + "'");
        }
        return dataHashMap.get(algorithm);
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public List<DataHash> getDataHashList(List<HashAlgorithm> algorithmList) {
        return new LinkedList<>(dataHashMap.values());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EmptyContainerDocument that = (EmptyContainerDocument) o;

        if (fileName != null ? !fileName.equals(that.fileName) : that.fileName != null) return false;
        if (mimeType != null ? !mimeType.equals(that.mimeType) : that.mimeType != null) return false;
        return dataHashMap != null ? dataHashMap.equals(that.dataHashMap) : that.dataHashMap == null;

    }

    @Override
    public int hashCode() {
        int result = fileName != null ? fileName.hashCode() : 0;
        result = 31 * result + (mimeType != null ? mimeType.hashCode() : 0);
        result = 31 * result + (dataHashMap != null ? dataHashMap.hashCode() : 0);
        return result;
    }

    @Override
    public void close() throws IOException {
        //Nothing to do here, no resources held.
    }
}
