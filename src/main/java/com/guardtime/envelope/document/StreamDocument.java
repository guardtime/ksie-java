/*
 * Copyright 2013-2018 Guardtime, Inc.
 *
 * This file is part of the Guardtime client SDK.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES, CONDITIONS, OR OTHER LICENSES OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * "Guardtime" and "KSI" are trademarks or registered trademarks of
 * Guardtime, Inc., and no license to trademarks is granted; Guardtime
 * reserves and retains all trademark rights.
 */

package com.guardtime.envelope.document;

import com.guardtime.envelope.util.DataHashException;
import com.guardtime.envelope.util.Util;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import static com.guardtime.envelope.util.Util.createTempFile;
import static com.guardtime.envelope.util.Util.notNull;

/**
 * Represents a {@link Document} that is based on a {@link InputStream}.
 */
public class StreamDocument implements Document {

    private final File tempFile;
    private FileDocument envelopeDocument;
    private boolean closed;

    /**
     * Creates {@link Document} with provided MIME-type and file name. Uses provided {@link InputStream} as data source.
     * @param input     The data source of the {@link Document}.
     * @param mimeType  The MIME-type of the {@link Document}.
     * @param fileName  The file name to be used for the {@link Document}.
     */
    public StreamDocument(InputStream input, String mimeType, String fileName) {
        notNull(input, "Input stream");
        notNull(mimeType, "MIME type");
        notNull(fileName, "File name");
        this.tempFile = copy(input);
        this.envelopeDocument = new FileDocument(tempFile, mimeType, fileName);
    }

    @Override
    public String getFileName() {
        return envelopeDocument.getFileName();
    }

    @Override
    public String getMimeType() {
        return envelopeDocument.getMimeType();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        checkClosed();
        return envelopeDocument.getInputStream();
    }

    @Override
    public String getPath() {
        return envelopeDocument.getPath();
    }

    @Override
    public DataHash getDataHash(HashAlgorithm algorithm) throws DataHashException {
        checkClosed();
        return envelopeDocument.getDataHash(algorithm);
    }

    @Override
    public List<DataHash> getDataHashList(List<HashAlgorithm> algorithmList) throws DataHashException {
        checkClosed();
        return envelopeDocument.getDataHashList(algorithmList);
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

        StreamDocument that = (StreamDocument) o;

        return envelopeDocument != null ? envelopeDocument.equals(that.envelopeDocument) : that.envelopeDocument == null;

    }

    @Override
    public int hashCode() {
        return envelopeDocument != null ? envelopeDocument.hashCode() : 0;
    }

    @Override
    public void close() throws Exception {
        envelopeDocument.close();
        Files.deleteIfExists(tempFile.toPath());
        this.closed = true;
    }

    private void checkClosed() {
        if (closed) {
            throw new IllegalStateException("Can't access closed document!");
        }
    }
}
