package com.guardtime.container.annotation;

import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static com.guardtime.container.util.Util.createTempFile;
import static com.guardtime.container.util.Util.notNull;

public class StreamContainerAnnotation implements ContainerAnnotation {

    private final File tempFile;
    private FileContainerAnnotation containerAnnotation;
    private boolean closed;

    public StreamContainerAnnotation(InputStream annotationStream, String domain, ContainerAnnotationType type) {
        notNull(annotationStream, "Input stream");
        notNull(domain, "Domain");
        notNull(type, "Type");
        this.tempFile = copy(annotationStream);
        this.containerAnnotation = new FileContainerAnnotation(tempFile, domain, type);

    }

    @Override
    public ContainerAnnotationType getAnnotationType() {
        return containerAnnotation.getAnnotationType();
    }

    @Override
    public String getDomain() {
        return containerAnnotation.getDomain();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        checkClosed();
        return containerAnnotation.getInputStream();
    }

    @Override
    public DataHash getDataHash(HashAlgorithm algorithm) throws IOException {
        checkClosed();
        return containerAnnotation.getDataHash(algorithm);
    }

    @Override
    public void close() throws Exception {
        containerAnnotation.close();
        Files.deleteIfExists(tempFile.toPath());
        this.closed = true;
    }

    private File copy(InputStream input) {
        try {
            File tempFile = createTempFile();
            Util.copyToTempFile(input, tempFile);
            return tempFile;
        } catch (IOException e) {
            throw new IllegalArgumentException("Can not copy input stream", e);
        }
    }

    private void checkClosed() throws IOException {
        if(closed) {
            throw new IOException("Can't access closed document!");
        }
    }
}
