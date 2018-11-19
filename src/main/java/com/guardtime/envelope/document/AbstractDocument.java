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
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.guardtime.envelope.manifest.Manifest.DEFAULT_HASH_ALGORITHM;
import static com.guardtime.envelope.util.Util.hash;
import static com.guardtime.envelope.util.Util.notNull;
import static com.guardtime.ksi.util.Util.toByteArray;


/**
 * Generic implementation for {@link Document} that is lacking {@link Document#getInputStream()} implementation.
 */
abstract class AbstractDocument implements Document {

    protected final String mimeType;
    protected final String fileName;
    protected boolean closed = false;

    /**
     *
     * Creates {@link Document} with provided MIME-type and file name.
     * @param mimeType The MIME-type of the {@link Document}.
     * @param fileName The file name to be used for the {@link Document}.
     */
    protected AbstractDocument(String mimeType, String fileName) {
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
    public DataHash getDataHash(HashAlgorithm algorithm) throws DataHashException {
        try (InputStream inputStream = getInputStream()) {
            return hash(inputStream, algorithm);
        } catch (IOException e) {
            throw new DataHashException("Failed to access data to generate hash.", e);
        }
    }

    @Override
    public List<DataHash> getDataHashList(List<HashAlgorithm> algorithmList) throws DataHashException {
        notNull(algorithmList, "Hash algorithm list");
        List<DataHash> hashList = new ArrayList<>();
        for (HashAlgorithm algorithm : algorithmList) {
            try {
                hashList.add(getDataHash(algorithm));
            } catch (DataHashException e) {
                // ignore as we don't care about single failure but rather the whole failure
            }
        }

        if (!algorithmList.isEmpty() && hashList.isEmpty()) {
            throw new DataHashException(
                    "Could not find any pre-generated hashes for requested algorithms! Algorithms requested: " + algorithmList
            );
        }
        return hashList;
    }

    @Override
    public boolean isWritable() {
        return !closed;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() +
                " {fileName= \'" + getFileName() + '\'' +
                ", mimeType= \'" + getMimeType() + "\'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (o instanceof EmptyDocument) return o.equals(this); // EmptyDocument should handle this!

        Document that = (Document) o;

        if (getFileName() != null ? !getFileName().equals(that.getFileName()) : that.getFileName() != null) return false;
        if (getMimeType() != null ? !getMimeType().equals(that.getMimeType()) : that.getMimeType() != null) return false;
        return doContentsMatch(this, that);
    }

    private boolean doContentsMatch(AbstractDocument abstractDocument, Document that) {
        try (InputStream thisStream = abstractDocument.getInputStream();
             InputStream thatStream = that.getInputStream()
        ) {
            return Arrays.equals(
                    toByteArray(thisStream),
                    toByteArray(thatStream)
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to compare content of Documents.", e);
        }
    }

    @Override
    public int hashCode() {
        try {
            int result = getDataHash(DEFAULT_HASH_ALGORITHM).hashCode();
            result = 31 * result + (mimeType != null ? mimeType.hashCode() : 0);
            result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
            return result;
        } catch (DataHashException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        //Nothing to do here
        this.closed = true;
    }

    @Override
    public String getPath() {
        return fileName;
    }

}
