package com.guardtime.container.document;

import com.guardtime.container.util.DataHashException;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.File;
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

    private final File tempFile;
    private FileContainerDocument containerDocument;
    private boolean closed;

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
        checkClosed();
        return containerDocument.getInputStream();
    }

    @Override
    public DataHash getDataHash(HashAlgorithm algorithm) throws DataHashException {
        checkClosed();
        return containerDocument.getDataHash(algorithm);
    }

    @Override
    public List<DataHash> getDataHashList(List<HashAlgorithm> algorithmList) throws IOException, DataHashException {
        checkClosed();
        return containerDocument.getDataHashList(algorithmList);
    }

    @Override
    public boolean isWritable() {
        return !closed;
    }

    protected File copy(InputStream input) {
        try {
            File tempFile = createTempFile();
            Util.copyToTempFile(input, tempFile);
            return tempFile;
        } catch (IOException e) {
            throw new IllegalArgumentException("Can not copy input stream", e);
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
    public void close() throws Exception {
        containerDocument.close();
        Files.deleteIfExists(tempFile.toPath());
        this.closed = true;
    }

    private void checkClosed() {
        if (closed) {
            throw new IllegalStateException("Can't access closed document!");
        }
    }
}
