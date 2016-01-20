package com.guardtime.container.util;

import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.DataHasher;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.hashing.HashException;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DigestOutputStream extends FilterOutputStream {

    private final DataHasher hasher;
    private boolean closed;

    public DigestOutputStream(OutputStream output, HashAlgorithm algorithm) throws HashException {
        super(output);
        this.hasher = new DataHasher(algorithm);
    }

    @Override
    public void write(int b) throws IOException {
        super.write(b);
        try {
            hasher.addData(new byte[] {(byte) b});
        } catch (HashException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.closed = true;
    }

    public DataHash getDataHash() throws HashException {
        if (!closed) {
            throw new IllegalStateException("Stream must be closed to get hash");
        }
        return hasher.getHash();
    }
}


