package com.guardtime.container.document;

import com.guardtime.container.util.DataHashException;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.guardtime.container.util.Util.notNull;

public abstract class AbstractContainerDocument implements ContainerDocument {

    public static final HashAlgorithm HASH_ALGORITHM = HashAlgorithm.SHA2_256;

    protected final String mimeType;
    protected final String fileName;

    protected AbstractContainerDocument(String mimeType, String fileName) {
        notNull(mimeType, "MIME type");
        notNull(fileName, "File name");
        this.mimeType = mimeType;
        this.fileName = fileName;
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
    public DataHash getDataHash(HashAlgorithm algorithm) throws IOException, DataHashException {
        try (InputStream inputStream = getInputStream()) {
            return Util.hash(inputStream, algorithm);
        }
    }

    @Override
    public List<DataHash> getDataHashList(List<HashAlgorithm> algorithmList) throws IOException, DataHashException {
        Util.notNull(algorithmList, "Hash algorithm list");
        List<DataHash> hashList = new ArrayList<>();
        for (HashAlgorithm algorithm : algorithmList) {
            try {
                hashList.add(getDataHash(algorithm));
            } catch (DataHashException e) {
                // ignore as we don't care about single failure but rather the whole failure
            }
        }

        if (!algorithmList.isEmpty() && hashList.isEmpty()) {
            throw new DataHashException("Could not find any pre-generated hashes for requested algorithms! Algorithms requested: " + algorithmList);
        }
        return hashList;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public String toString() {
        return this.getClass().toString() +
                " {fileName=\'" + fileName +
                "\', mimeType=\'" + mimeType + "\'}";
    }

    @Override
    public boolean equals(Object o) {
        try {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FileContainerDocument that = (FileContainerDocument) o;

            if (fileName != null ? !fileName.equals(that.fileName) : that.fileName != null) return false;
            if (mimeType != null ? !mimeType.equals(that.mimeType) : that.mimeType != null) return false;
            return this.getDataHash(HASH_ALGORITHM).equals(that.getDataHash(HASH_ALGORITHM));
        } catch (IOException | DataHashException e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result;
        try {
            result = getDataHash(HASH_ALGORITHM).hashCode();
        } catch (IOException | DataHashException e) {
            result = 0;
        }
        result = 31 * result + (mimeType != null ? mimeType.hashCode() : 0);
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        return result;
    }

    @Override
    public void close() throws Exception {
        //Nothing to do here
    }

}
