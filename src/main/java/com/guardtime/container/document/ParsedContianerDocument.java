package com.guardtime.container.document;

import com.guardtime.container.packaging.parsing.ParsedStreamProvider;
import com.guardtime.container.packaging.parsing.ParsingStoreException;
import com.guardtime.container.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.guardtime.container.util.Util.notNull;

public class ParsedContianerDocument implements ContainerDocument {

    private final ParsedStreamProvider streamProvider;
    private final String mimeType;
    private final String fileName;
    private boolean closed;

    public ParsedContianerDocument(ParsedStreamProvider streamProvider, String mimeType, String fileName) {
        notNull(streamProvider, "Stream provider");
        notNull(mimeType, "MIME type");
        notNull(fileName, "File name");
        this.streamProvider = streamProvider;
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
    public InputStream getInputStream() throws IOException {
        checkClosed();
        try {
            return streamProvider.getNewStream();
        } catch (ParsingStoreException e) {
            throw new IOException("Failed to acquire stream", e);
        }
    }

    @Override
    public DataHash getDataHash(HashAlgorithm algorithm) throws IOException {
        checkClosed();
        try (InputStream inputStream = getInputStream()) {
            return Util.hash(inputStream, algorithm);
        }
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
        return !closed;
    }

    @Override
    public void close() throws Exception {
        streamProvider.close();
        this.closed = true;
    }

    private void checkClosed() throws IOException {
        if (closed) {
            throw new IOException("Can't access closed document!");
        }
    }
}
