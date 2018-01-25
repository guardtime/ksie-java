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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.guardtime.envelope.util.Util.notNull;

public abstract class AbstractDocument implements Document {

    public static final HashAlgorithm HASH_ALGORITHM = HashAlgorithm.SHA2_256;

    protected final String mimeType;
    protected final String fileName;

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
            return Util.hash(inputStream, algorithm);
        } catch (IOException e) {
            throw new DataHashException("Failed to access data to generate hash.", e);
        }
    }

    @Override
    public List<DataHash> getDataHashList(List<HashAlgorithm> algorithmList) throws DataHashException {
        Util.notNull(algorithmList, "Hash algorithm list");
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
        return true;
    }

    @Override
    public String toString() {
        return this.getClass().toString() +
                " {fileName=\'" + fileName +
                "\', mimeType=\'" + mimeType + "\'}";
    }

    @Override
    public boolean equals(Object o) {
        try {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Document that = (Document) o;

            if (getFileName() != null ? !getFileName().equals(that.getFileName()) : that.getFileName() != null) return false;
            if (getMimeType() != null ? !getMimeType().equals(that.getMimeType()) : that.getMimeType() != null) return false;
            return this.getDataHash(HASH_ALGORITHM).equals(that.getDataHash(HASH_ALGORITHM));
        } catch (DataHashException e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result;
        try {
            result = getDataHash(HASH_ALGORITHM).hashCode();
        } catch (DataHashException e) {
            result = 0;
        }
        result = 31 * result + (mimeType != null ? mimeType.hashCode() : 0);
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        return result;
    }

    @Override
    public void close() throws Exception {
        //Nothing to do here
    }

    @Override
    public String getPath() {
        return fileName;
    }
}
