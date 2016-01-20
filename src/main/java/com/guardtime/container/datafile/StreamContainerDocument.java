package com.guardtime.container.datafile;

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.guardtime.container.util.Util.createTempFile;
import static com.guardtime.container.util.Util.notNull;

public class StreamContainerDocument implements ContainerDocument {

    private static final String TEMP_FILE_PREFIX = "bcc-";
    private static final String TEMP_FILE_SUFFIX = ".dat";

    private FileContainerDocument documentContent;

    public StreamContainerDocument(InputStream input, String fileName, String mimeType) {
        notNull(input, "Input stream");
        notNull(fileName, "File name");
        notNull(mimeType, "MIME type");
        File tempFile = copy(input);
        this.documentContent = new FileContainerDocument(tempFile, mimeType, fileName);
    }

    @Override
    public String getFileName() {
        return documentContent.getFileName();
    }

    @Override
    public String getMimeType() {
        return documentContent.getMimeType();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return documentContent.getInputStream();
    }

    @Override
    public DataHash getDataHash(HashAlgorithm algorithm) throws IOException {
        return documentContent.getDataHash(algorithm);
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

}
