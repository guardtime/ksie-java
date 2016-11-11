package com.guardtime.container.document;

import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.guardtime.container.util.Util.notNull;

/**
 * Document that is based on a {@link File}.
 */
public class FileContainerDocument implements ContainerDocument {

    public static final HashAlgorithm HASH_ALGORITHM = HashAlgorithm.SHA2_256;
    private final File file;
    private final String mimeType;
    private final String fileName;

    public FileContainerDocument(File file, String mimeType) {
        this(file, mimeType, null);
    }

    public FileContainerDocument(File file, String mimeType, String fileName) {
        notNull(file, "File");
        notNull(mimeType, "MIME type");
        this.file = file;
        this.mimeType = mimeType;
        this.fileName = fileName == null ? file.getName() : fileName;
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
        return new FileInputStream(file);
    }

    @Override
    public DataHash getDataHash(HashAlgorithm algorithm) throws IOException {
        return Util.hash(getInputStream(), algorithm);
    }

    @Override
    public List<DataHash> getDataHashList(List<HashAlgorithm> algorithmList) throws IOException {
        List<DataHash> hashList = new ArrayList<>();
        for (HashAlgorithm algorithm : algorithmList) {
            hashList.add(getDataHash(algorithm));
        }
        return hashList;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public String toString() {
        return "{type=File" +
                ", fileName=" + fileName +
                ", mimeType=" + mimeType + "}";
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
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result;
        try {
            result = getDataHash(HASH_ALGORITHM).hashCode();
        } catch (IOException e) {
            result = file != null ? file.hashCode() : 0;
        }
        result = 31 * result + (mimeType != null ? mimeType.hashCode() : 0);
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        return result;
    }

    @Override
    public void close() {
        //Nothing to do here, we don't know where the input file is from so can't delete it.
    }
}
