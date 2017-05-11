/*
 * Copyright 2013-2017 Guardtime, Inc.
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

    @Override
    public String toString() {
        return "{type=Stream" +
                ", fileName=" + containerDocument.getFileName() +
                ", mimeType=" + containerDocument.getMimeType() + "}";
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
