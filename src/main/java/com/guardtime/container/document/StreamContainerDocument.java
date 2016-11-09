package com.guardtime.container.document;

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import static com.guardtime.container.util.Util.createTempFile;
import static com.guardtime.container.util.Util.notNull;

/**
 * Document that is based on a {@link InputStream}.
 */
public class StreamContainerDocument implements ContainerDocument {

    private static final String TEMP_FILE_PREFIX = "bcc-";
    private static final String TEMP_FILE_SUFFIX = ".dat";
    private final File tempFile;
    private FileContainerDocument containerDocument;

    public StreamContainerDocument(InputStream input, String mimeType, String fileName) {
        notNull(input, "Input stream");
        notNull(mimeType, "MIME type");
        notNull(fileName, "File name");
        this.tempFile = copy(input);
        this.containerDocument = new FileContainerDocument(tempFile, mimeType, fileName);
    }

    @Override
    public String getFileName() {
        return containerDocument.getFileName();
    }

    @Override
    public String getMimeType() {
        return containerDocument.getMimeType();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return containerDocument.getInputStream();
    }

    @Override
    public DataHash getDataHash(HashAlgorithm algorithm) throws IOException {
        return containerDocument.getDataHash(algorithm);
    }

    @Override
    public List<DataHash> getDataHashList(List<HashAlgorithm> algorithmList) throws IOException {
        return containerDocument.getDataHashList(algorithmList);
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public String toString() {
        return "{type=Stream" +
                ", fileName=" + containerDocument.getFileName() +
                ", mimeType=" + containerDocument.getMimeType() + "}";
    }

    protected File copy(InputStream input) {
        FileOutputStream output = null;
        try {
            File tempFile = createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
            output = new FileOutputStream(tempFile);
            com.guardtime.ksi.util.Util.copyData(input, output);
            return tempFile;
        } catch (IOException e) {
            throw new IllegalArgumentException("Can not copy input stream", e);
        } finally {
            com.guardtime.ksi.util.Util.closeQuietly(output);
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StreamContainerDocument that = (StreamContainerDocument) o;

        return containerDocument != null ? containerDocument.equals(that.containerDocument) : that.containerDocument == null;

    }

    @Override
    public int hashCode() {
        return containerDocument != null ? containerDocument.hashCode() : 0;
    }

    @Override
    public void close() throws IOException {
        containerDocument.close();
        Files.deleteIfExists(tempFile.toPath());
    }

    @Override
    protected void finalize() throws Throwable {
        close();
    }
}
